/**
 * ERRORCab - Commercial Simulated GPS Map & Telemetry Canvas Engine
 * Kochi / Kerala Urban Road Network & Navigation Visualizer
 * High-performance HTML5 Canvas renderer with multi-tier road networks,
 * water bodies, animated glowing route paths, beacon radar pulses,
 * directional cab heading with headlight projection, and in-map telemetry HUD.
 */

class SimulatedMap {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        if (!this.canvas) return;
        this.ctx = this.canvas.getContext('2d');

        this.pickup = 'Kakkanad';
        this.destination = 'Vyttila';
        this.distanceText = '9.2 km';
        this.durationText = '24 min';
        this.progress = 0.0;
        this.animationFrame = null;
        this.pulseTime = 0;
        this.dashOffset = 0;

        // Kochi Landmark Coordinates mapped on 600x400 normalized space
        this.locations = {
            'Kakkanad': { x: 470, y: 135, name: 'Kakkanad', landmark: 'Infopark IT Hub', type: 'tech' },
            'Edappally': { x: 335, y: 95, name: 'Edappally', landmark: 'Lulu Mall & Metro', type: 'commercial' },
            'Palarivattom': { x: 330, y: 155, name: 'Palarivattom', landmark: 'Civil Line Junction', type: 'junction' },
            'Kaloor': { x: 285, y: 195, name: 'Kaloor', landmark: 'JLN Stadium', type: 'metro' },
            'Vyttila': { x: 365, y: 245, name: 'Vyttila', landmark: 'Mobility Hub', type: 'transit' },
            'MG Road': { x: 220, y: 235, name: 'MG Road', landmark: 'Commercial Blvd', type: 'commercial' },
            'Ernakulam South': { x: 240, y: 275, name: 'Ernakulam South', landmark: 'Central Railway', type: 'transit' },
            'Thrippunithura': { x: 460, y: 295, name: 'Thrippunithura', landmark: 'Hill Palace', type: 'heritage' },
            'Fort Kochi': { x: 85, y: 265, name: 'Fort Kochi', landmark: 'Chinese Fishing Nets', type: 'coastal' },
            'Aluva': { x: 375, y: 32, name: 'Aluva', landmark: 'Periyar River Hub', type: 'transit' },
            'Trivandrum': { x: 305, y: 375, name: 'Trivandrum', landmark: 'Technopark Hub', type: 'metro' },
            'Kozhikode': { x: 175, y: 40, name: 'Kozhikode', landmark: 'Beach Road Hub', type: 'coastal' }
        };

        this.waypoints = [];
        this.resize();

        // Responsive resize listener
        window.addEventListener('resize', () => this.resize());
        
        // Theme listener for reactive map surface updates
        window.addEventListener('themeChanged', () => this.render());

        // Continuous subtle beacon radar & flow animation
        this.startBeaconLoop();
    }

    startBeaconLoop() {
        const loop = (timestamp) => {
            this.pulseTime = (timestamp / 1000) % 3.0; // 0 to 3 seconds cycle
            this.dashOffset = (timestamp / 50) % 24;   // Route flowing dash offset

            // If not actively animating trip steps, redraw beacon pulses
            if (!this.isTripAnimating) {
                this.render();
            }
            this.beaconFrame = requestAnimationFrame(loop);
        };
        this.beaconFrame = requestAnimationFrame(loop);
    }

    resize() {
        if (!this.canvas) return;
        const rect = this.canvas.parentElement ? this.canvas.parentElement.getBoundingClientRect() : this.canvas.getBoundingClientRect();
        const dpr = window.devicePixelRatio || 1;
        const displayWidth = rect.width || 600;
        const displayHeight = rect.height || 420;

        this.canvas.width = displayWidth * dpr;
        this.canvas.height = displayHeight * dpr;
        this.canvas.style.width = displayWidth + 'px';
        this.canvas.style.height = displayHeight + 'px';

        if (this.ctx) {
            this.ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        }

        this.width = displayWidth;
        this.height = displayHeight;

        this.updateWaypoints();
        this.render();
    }

    setRoute(pickup, destination, distanceKm = null, durationMins = null) {
        this.pickup = pickup || 'Kakkanad';
        this.destination = destination || 'Vyttila';
        this.progress = 0.0;

        if (distanceKm !== null && distanceKm !== undefined) {
            this.distanceText = (Math.round(distanceKm * 10) / 10).toFixed(1) + ' km';
        }
        if (durationMins !== null && durationMins !== undefined) {
            this.durationText = Math.round(durationMins) + ' min';
        }

        this.updateWaypoints();
        this.render();
    }

    updateTelemetry(distanceKm, durationMins) {
        if (distanceKm !== null && distanceKm !== undefined) {
            this.distanceText = (Math.round(distanceKm * 10) / 10).toFixed(1) + ' km';
        }
        if (durationMins !== null && durationMins !== undefined) {
            this.durationText = Math.round(durationMins) + ' min';
        }
        this.render();
    }

    updateWaypoints() {
        const p1 = this.locations[this.pickup] || this.locations['Kakkanad'];
        const p2 = this.locations[this.destination] || this.locations['Vyttila'];

        const midX = (p1.x + p2.x) / 2.0;
        const midY = (p1.y + p2.y) / 2.0;
        const jitterX = (p2.y - p1.y) * 0.14;
        const jitterY = (p1.x - p2.x) * 0.14;

        this.waypoints = [
            { x: p1.x, y: p1.y },
            { x: midX + jitterX, y: midY + jitterY },
            { x: p2.x, y: p2.y }
        ];
    }

    setCarProgress(t) {
        this.progress = Math.max(0.0, Math.min(1.0, t));
        this.render();
    }

    animateTrip(durationSeconds = 5.0, onComplete = null) {
        if (this.animationFrame) {
            cancelAnimationFrame(this.animationFrame);
        }

        this.isTripAnimating = true;
        const startTime = performance.now();
        const durationMs = durationSeconds * 1000;

        const frame = (now) => {
            const elapsed = now - startTime;
            const t = Math.min(1.0, elapsed / durationMs);
            this.setCarProgress(t);

            if (t < 1.0) {
                this.animationFrame = requestAnimationFrame(frame);
            } else {
                this.isTripAnimating = false;
                if (onComplete) onComplete();
            }
        };

        this.animationFrame = requestAnimationFrame(frame);
    }

    // Bezier curve point interpolation with tangent rotation
    getBezierPoint(p0, p1, p2, t) {
        const oneMinusT = 1.0 - t;
        const x = oneMinusT * oneMinusT * p0.x + 2.0 * oneMinusT * t * p1.x + t * t * p2.x;
        const y = oneMinusT * oneMinusT * p0.y + 2.0 * oneMinusT * t * p1.y + t * t * p2.y;

        const dx = 2.0 * (oneMinusT) * (p1.x - p0.x) + 2.0 * t * (p2.x - p1.x);
        const dy = 2.0 * (oneMinusT) * (p1.y - p0.y) + 2.0 * t * (p2.y - p1.y);
        const angle = Math.atan2(dy, dx);

        return { x, y, angle };
    }

    render() {
        if (!this.ctx || !this.canvas) return;
        const ctx = this.ctx;
        const w = this.width || 600;
        const h = this.height || 420;

        const isDark = document.documentElement.getAttribute('data-theme') !== 'light';

        const scaleX = w / 600.0;
        const scaleY = h / 400.0;
        const minScale = Math.min(scaleX, scaleY);

        ctx.clearRect(0, 0, w, h);

        // 1. High-Tech Navigation Canvas Surface
        ctx.fillStyle = isDark ? '#0B1120' : '#F1F5F9';
        ctx.fillRect(0, 0, w, h);

        // 2. Subtle Cartography Coordinate Grid
        ctx.strokeStyle = isDark ? 'rgba(255, 255, 255, 0.025)' : 'rgba(15, 23, 42, 0.04)';
        ctx.lineWidth = 1;
        const gridSize = 45;
        for (let x = 0; x < w; x += gridSize) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, h);
            ctx.stroke();
        }
        for (let y = 0; y < h; y += gridSize) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(w, y);
            ctx.stroke();
        }

        // 3. Kochi Waterway & Arabian Sea Coastal Contours
        // Main coastal bay / Arabian Sea
        ctx.fillStyle = isDark ? '#081D33' : '#E0F2FE';
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.bezierCurveTo(75 * scaleX, 100 * scaleY, 45 * scaleX, 220 * scaleY, 0, 400 * scaleY);
        ctx.lineTo(0, 0);
        ctx.fill();

        // Coastal Waterline Accent
        ctx.strokeStyle = isDark ? 'rgba(56, 189, 248, 0.18)' : 'rgba(2, 132, 199, 0.2)';
        ctx.lineWidth = 1.5;
        ctx.stroke();

        // Vembanad Lake / Backwaters Estuary
        ctx.fillStyle = isDark ? '#0A2542' : '#BAE6FD';
        ctx.beginPath();
        ctx.moveTo(110 * scaleX, 170 * scaleY);
        ctx.bezierCurveTo(155 * scaleX, 215 * scaleY, 195 * scaleX, 285 * scaleY, 175 * scaleX, 360 * scaleY);
        ctx.lineTo(135 * scaleX, 360 * scaleY);
        ctx.bezierCurveTo(145 * scaleX, 275 * scaleY, 115 * scaleX, 215 * scaleY, 80 * scaleX, 180 * scaleY);
        ctx.closePath();
        ctx.fill();

        // 4. City District Zones (Subtle Tech & Commercial Enclaves)
        // Infopark IT Zone
        ctx.fillStyle = isDark ? 'rgba(56, 189, 248, 0.04)' : 'rgba(2, 132, 199, 0.06)';
        ctx.beginPath();
        ctx.roundRect(430 * scaleX, 100 * scaleY, 85 * scaleX, 70 * scaleY, 12 * minScale);
        ctx.fill();
        ctx.strokeStyle = isDark ? 'rgba(56, 189, 248, 0.12)' : 'rgba(2, 132, 199, 0.12)';
        ctx.lineWidth = 1;
        ctx.stroke();

        ctx.fillStyle = isDark ? 'rgba(148, 163, 184, 0.4)' : 'rgba(100, 116, 139, 0.5)';
        ctx.font = `800 ${Math.max(8, 9 * minScale)}px 'Plus Jakarta Sans', sans-serif`;
        ctx.fillText('INFOPARK TECH ZONE', 438 * scaleX, 115 * scaleY);

        // Fort Kochi Heritage Zone
        ctx.fillStyle = isDark ? 'rgba(245, 158, 11, 0.03)' : 'rgba(217, 119, 6, 0.05)';
        ctx.beginPath();
        ctx.roundRect(55 * scaleX, 230 * scaleY, 70 * scaleX, 60 * scaleY, 10 * minScale);
        ctx.fill();

        // 5. Urban Road Network (Multi-Tier Arterials & Highways)
        const majorHighways = [
            // NH 66 Bypass Corridor
            [{ x: 375, y: 32 }, { x: 335, y: 95 }, { x: 330, y: 155 }, { x: 365, y: 245 }, { x: 460, y: 295 }],
            // Seaport-Airport Road
            [{ x: 375, y: 32 }, { x: 470, y: 135 }, { x: 460, y: 295 }],
            // Palarivattom - Kakkanad Link
            [{ x: 330, y: 155 }, { x: 470, y: 135 }],
            // Kaloor - MG Road Boulevard
            [{ x: 335, y: 95 }, { x: 285, y: 195 }, { x: 220, y: 235 }, { x: 240, y: 275 }],
            // Vyttila - SA Road - MG Road Link
            [{ x: 365, y: 245 }, { x: 240, y: 275 }],
            // Fort Kochi Link
            [{ x: 220, y: 235 }, { x: 85, y: 265 }]
        ];

        // Highway Outer Border / Casing
        ctx.strokeStyle = isDark ? '#1E293B' : '#CBD5E1';
        ctx.lineWidth = 7 * minScale;
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';
        majorHighways.forEach(r => {
            ctx.beginPath();
            ctx.moveTo(r[0].x * scaleX, r[0].y * scaleY);
            for (let i = 1; i < r.length; i++) {
                ctx.lineTo(r[i].x * scaleX, r[i].y * scaleY);
            }
            ctx.stroke();
        });

        // Highway Inner Surface
        ctx.strokeStyle = isDark ? '#334155' : '#E2E8F0';
        ctx.lineWidth = 4 * minScale;
        majorHighways.forEach(r => {
            ctx.beginPath();
            ctx.moveTo(r[0].x * scaleX, r[0].y * scaleY);
            for (let i = 1; i < r.length; i++) {
                ctx.lineTo(r[i].x * scaleX, r[i].y * scaleY);
            }
            ctx.stroke();
        });

        // Kochi Metro Rapid Transit Line (Cyan Dashed Line)
        const metroLine = [
            { x: 375, y: 32 }, { x: 335, y: 95 }, { x: 330, y: 155 },
            { x: 285, y: 195 }, { x: 220, y: 235 }, { x: 240, y: 275 }, { x: 365, y: 245 }
        ];
        ctx.strokeStyle = isDark ? 'rgba(56, 189, 248, 0.45)' : 'rgba(2, 132, 199, 0.45)';
        ctx.lineWidth = 2 * minScale;
        ctx.setLineDash([4 * minScale, 4 * minScale]);
        ctx.beginPath();
        ctx.moveTo(metroLine[0].x * scaleX, metroLine[0].y * scaleY);
        for (let i = 1; i < metroLine.length; i++) {
            ctx.lineTo(metroLine[i].x * scaleX, metroLine[i].y * scaleY);
        }
        ctx.stroke();
        ctx.setLineDash([]); // Reset line dash

        // 6. Active Trip Route (Vibrant Flowing Gradient Line)
        if (this.waypoints.length === 3) {
            const w0 = { x: this.waypoints[0].x * scaleX, y: this.waypoints[0].y * scaleY };
            const w1 = { x: this.waypoints[1].x * scaleX, y: this.waypoints[1].y * scaleY };
            const w2 = { x: this.waypoints[2].x * scaleX, y: this.waypoints[2].y * scaleY };

            // Outer Path Glow
            ctx.strokeStyle = isDark ? 'rgba(14, 165, 233, 0.28)' : 'rgba(2, 132, 199, 0.22)';
            ctx.lineWidth = 12 * minScale;
            ctx.lineCap = 'round';
            ctx.beginPath();
            ctx.moveTo(w0.x, w0.y);
            ctx.quadraticCurveTo(w1.x, w1.y, w2.x, w2.y);
            ctx.stroke();

            // Core Electric Route Line
            const routeGrad = ctx.createLinearGradient(w0.x, w0.y, w2.x, w2.y);
            routeGrad.addColorStop(0, '#10B981'); // Emerald at Pickup
            routeGrad.addColorStop(0.5, '#0EA5E9');
            routeGrad.addColorStop(1, '#0284C7'); // Deep Blue at Dropoff

            ctx.strokeStyle = routeGrad;
            ctx.lineWidth = 4.5 * minScale;
            ctx.beginPath();
            ctx.moveTo(w0.x, w0.y);
            ctx.quadraticCurveTo(w1.x, w1.y, w2.x, w2.y);
            ctx.stroke();

            // Flowing Animated Energy Dashes along the Route
            ctx.strokeStyle = '#FFFFFF';
            ctx.lineWidth = 2 * minScale;
            ctx.setLineDash([6 * minScale, 18 * minScale]);
            ctx.lineDashOffset = -this.dashOffset;
            ctx.beginPath();
            ctx.moveTo(w0.x, w0.y);
            ctx.quadraticCurveTo(w1.x, w1.y, w2.x, w2.y);
            ctx.stroke();
            ctx.setLineDash([]);
            ctx.lineDashOffset = 0;

            // Sleek Directional Cab Marker
            const pos = this.getBezierPoint(w0, w1, w2, this.progress);
            this.drawCarMarker(ctx, pos.x, pos.y, pos.angle, minScale, isDark);
        }

        // 7. City Landmark Nodes & High-End Markers
        Object.values(this.locations).forEach(loc => {
            const lx = loc.x * scaleX;
            const ly = loc.y * scaleY;
            const isPickup = loc.name === this.pickup;
            const isDest = loc.name === this.destination;

            if (isPickup) {
                // Pulsing Emerald Beacon Rings
                const pulseRadius = 12 * minScale + (this.pulseTime * 8 * minScale);
                const pulseAlpha = Math.max(0, 0.4 - (this.pulseTime * 0.13));

                ctx.fillStyle = `rgba(16, 185, 129, ${pulseAlpha})`;
                ctx.beginPath();
                ctx.arc(lx, ly, pulseRadius, 0, Math.PI * 2);
                ctx.fill();

                // Inner Glow Halo
                ctx.fillStyle = 'rgba(16, 185, 129, 0.35)';
                ctx.beginPath();
                ctx.arc(lx, ly, 11 * minScale, 0, Math.PI * 2);
                ctx.fill();

                // Solid Core Pin
                ctx.fillStyle = '#10B981';
                ctx.strokeStyle = '#FFFFFF';
                ctx.lineWidth = 2 * minScale;
                ctx.beginPath();
                ctx.arc(lx, ly, 6 * minScale, 0, Math.PI * 2);
                ctx.fill();
                ctx.stroke();

                // Floating Drop-Shadow Pill Label
                this.drawPillBadge(ctx, lx, ly - 18 * minScale, `📍 Pickup: ${loc.name}`, '#10B981', isDark, minScale);

            } else if (isDest) {
                // Cyan Beacon Ring
                const pulseRadius = 12 * minScale + (((this.pulseTime + 1.5) % 3.0) * 8 * minScale);
                const pulseAlpha = Math.max(0, 0.35 - (((this.pulseTime + 1.5) % 3.0) * 0.12));

                ctx.fillStyle = `rgba(14, 165, 233, ${pulseAlpha})`;
                ctx.beginPath();
                ctx.arc(lx, ly, pulseRadius, 0, Math.PI * 2);
                ctx.fill();

                // Inner Halo
                ctx.fillStyle = 'rgba(14, 165, 233, 0.35)';
                ctx.beginPath();
                ctx.arc(lx, ly, 11 * minScale, 0, Math.PI * 2);
                ctx.fill();

                // Solid Core Pin (Square for Destination)
                ctx.fillStyle = '#0284C7';
                ctx.strokeStyle = '#FFFFFF';
                ctx.lineWidth = 2 * minScale;
                ctx.beginPath();
                ctx.roundRect(lx - 5 * minScale, ly - 5 * minScale, 10 * minScale, 10 * minScale, 2 * minScale);
                ctx.fill();
                ctx.stroke();

                // Floating Pill Label
                this.drawPillBadge(ctx, lx, ly - 18 * minScale, `🏁 Destination: ${loc.name}`, '#0284C7', isDark, minScale);

            } else {
                // Background Junction Node
                ctx.fillStyle = isDark ? '#334155' : '#94A3B8';
                ctx.beginPath();
                ctx.arc(lx, ly, 3 * minScale, 0, Math.PI * 2);
                ctx.fill();

                ctx.fillStyle = isDark ? '#64748B' : '#64748B';
                ctx.font = `600 ${Math.max(9, 9.5 * minScale)}px 'Plus Jakarta Sans', sans-serif`;
                ctx.fillText(loc.name, lx + 7 * minScale, ly + 3.5 * minScale);
            }
        });

        // 8. In-Map Telemetry HUD Overlay (Glassmorphic Pill)
        this.drawMapHUD(ctx, w, h, minScale, isDark);
    }

    // Modern Floating Pill Badge
    drawPillBadge(ctx, x, y, text, accentColor, isDark, minScale) {
        ctx.font = `800 ${Math.max(10, 11 * minScale)}px 'Plus Jakarta Sans', sans-serif`;
        const textWidth = ctx.measureText(text).width;
        const padX = 8 * minScale;
        const padY = 4 * minScale;
        const boxW = textWidth + padX * 2;
        const boxH = 20 * minScale;
        const boxX = Math.max(4, Math.min(this.width - boxW - 4, x - boxW / 2));
        const boxY = y - boxH / 2;

        // Shadow
        ctx.fillStyle = 'rgba(0, 0, 0, 0.35)';
        ctx.beginPath();
        ctx.roundRect(boxX, boxY + 2, boxW, boxH, 6 * minScale);
        ctx.fill();

        // Background
        ctx.fillStyle = isDark ? '#0F172A' : '#FFFFFF';
        ctx.strokeStyle = accentColor;
        ctx.lineWidth = 1.5 * minScale;
        ctx.beginPath();
        ctx.roundRect(boxX, boxY, boxW, boxH, 6 * minScale);
        ctx.fill();
        ctx.stroke();

        // Text
        ctx.fillStyle = isDark ? '#F8FAFC' : '#0F172A';
        ctx.fillText(text, boxX + padX, boxY + boxH - 6 * minScale);
    }

    // In-Map Telemetry HUD Card
    drawMapHUD(ctx, w, h, minScale, isDark) {
        const hudX = 14 * minScale;
        const hudY = 14 * minScale;
        const hudW = Math.min(270 * minScale, w - 28 * minScale);
        const hudH = 50 * minScale;

        // Backdrop
        ctx.fillStyle = isDark ? 'rgba(15, 23, 42, 0.88)' : 'rgba(255, 255, 255, 0.92)';
        ctx.strokeStyle = isDark ? 'rgba(56, 189, 248, 0.25)' : 'rgba(2, 132, 199, 0.2)';
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.roundRect(hudX, hudY, hudW, hudH, 10 * minScale);
        ctx.fill();
        ctx.stroke();

        // Live Pulse Indicator
        ctx.fillStyle = '#10B981';
        ctx.beginPath();
        ctx.arc(hudX + 12 * minScale, hudY + 14 * minScale, 3.5 * minScale, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = isDark ? '#94A3B8' : '#64748B';
        ctx.font = `800 ${Math.max(9, 9.5 * minScale)}px 'Plus Jakarta Sans', sans-serif`;
        ctx.fillText('SIMULATED GPS • KOCHI FLEET', hudX + 22 * minScale, hudY + 17 * minScale);

        // Telemetry Row: Distance & Travel Time
        ctx.fillStyle = isDark ? '#F8FAFC' : '#0F172A';
        ctx.font = `900 ${Math.max(11, 12 * minScale)}px 'Plus Jakarta Sans', sans-serif`;
        const metrics = `Distance: ${this.distanceText}  •  Est. Time: ${this.durationText}`;
        ctx.fillText(metrics, hudX + 12 * minScale, hudY + 37 * minScale);

        // Compass Rose in Top-Right
        const compX = w - 24 * minScale;
        const compY = 24 * minScale;
        ctx.fillStyle = isDark ? 'rgba(15, 23, 42, 0.75)' : 'rgba(255, 255, 255, 0.85)';
        ctx.beginPath();
        ctx.arc(compX, compY, 13 * minScale, 0, Math.PI * 2);
        ctx.fill();
        ctx.strokeStyle = isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)';
        ctx.stroke();

        ctx.fillStyle = '#EF4444'; // Red North needle
        ctx.font = `900 ${Math.max(8, 9 * minScale)}px 'Plus Jakarta Sans', sans-serif`;
        ctx.textAlign = 'center';
        ctx.fillText('N', compX, compY + 3.5 * minScale);
        ctx.textAlign = 'left'; // Reset
    }

    // Sleek Directional Cab Marker with Headlights
    drawCarMarker(ctx, x, y, angle, minScale, isDark) {
        ctx.save();
        ctx.translate(x, y);
        ctx.rotate(angle);

        // 1. Headlight Light Cones Beaming Forward
        const lightGrad = ctx.createRadialGradient(14 * minScale, 0, 2 * minScale, 38 * minScale, 0, 36 * minScale);
        lightGrad.addColorStop(0, 'rgba(253, 224, 71, 0.45)');
        lightGrad.addColorStop(1, 'rgba(253, 224, 71, 0.0)');

        ctx.fillStyle = lightGrad;
        ctx.beginPath();
        ctx.moveTo(14 * minScale, -5 * minScale);
        ctx.lineTo(46 * minScale, -16 * minScale);
        ctx.lineTo(46 * minScale, 16 * minScale);
        ctx.lineTo(14 * minScale, 5 * minScale);
        ctx.closePath();
        ctx.fill();

        // 2. Vehicle Soft Shadow
        ctx.fillStyle = 'rgba(0, 0, 0, 0.45)';
        ctx.beginPath();
        ctx.roundRect(-16 * minScale, -9 * minScale, 32 * minScale, 18 * minScale, 6 * minScale);
        ctx.fill();

        // 3. Vehicle Chassis
        ctx.fillStyle = '#0284C7';
        ctx.strokeStyle = '#FFFFFF';
        ctx.lineWidth = 1.5 * minScale;
        ctx.beginPath();
        ctx.roundRect(-15 * minScale, -9 * minScale, 30 * minScale, 18 * minScale, 5 * minScale);
        ctx.fill();
        ctx.stroke();

        // 4. Windshield Glass (Front & Rear)
        ctx.fillStyle = '#0F172A';
        // Front Windshield
        ctx.fillRect(2 * minScale, -7 * minScale, 6 * minScale, 14 * minScale);
        // Rear Windshield
        ctx.fillRect(-10 * minScale, -6 * minScale, 4 * minScale, 12 * minScale);

        // 5. Taxi Amber Roof Sign
        ctx.fillStyle = '#F59E0B';
        ctx.strokeStyle = '#FFFFFF';
        ctx.lineWidth = 0.8 * minScale;
        ctx.beginPath();
        ctx.roundRect(-4 * minScale, -4 * minScale, 7 * minScale, 8 * minScale, 2 * minScale);
        ctx.fill();
        ctx.stroke();

        // 6. Dual Headlights
        ctx.fillStyle = '#FEF08A';
        ctx.fillRect(13 * minScale, -8 * minScale, 2.5 * minScale, 4 * minScale);
        ctx.fillRect(13 * minScale, 4 * minScale, 2.5 * minScale, 4 * minScale);

        // 7. Tail Lights
        ctx.fillStyle = '#EF4444';
        ctx.fillRect(-15.5 * minScale, -7.5 * minScale, 1.5 * minScale, 3.5 * minScale);
        ctx.fillRect(-15.5 * minScale, 4 * minScale, 1.5 * minScale, 3.5 * minScale);

        ctx.restore();
    }
}
