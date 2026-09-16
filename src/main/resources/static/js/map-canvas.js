/**
 * ERRORCab - Commercial Simulated GPS Map & Telemetry Canvas Engine
 * Kochi / Kerala Road Network & Navigation Visualizer
 * High-performance HTML5 Canvas renderer with vector roads, waterways,
 * glowing route paths, and smooth car marker heading rotation.
 */

class SimulatedMap {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        if (!this.canvas) return;
        this.ctx = this.canvas.getContext('2d');

        this.pickup = 'Kakkanad';
        this.destination = 'Vyttila';
        this.progress = 0.0;
        this.animationFrame = null;
        this.pulseAngle = 0;

        // Kochi Landmark Coordinates mapped on 600x400 normalized space
        this.locations = {
            'Kakkanad': { x: 460, y: 140, name: 'Kakkanad', landmark: 'Infopark' },
            'Edappally': { x: 340, y: 90, name: 'Edappally', landmark: 'Lulu Mall' },
            'Palarivattom': { x: 330, y: 150, name: 'Palarivattom', landmark: 'Junction' },
            'Kaloor': { x: 290, y: 190, name: 'Kaloor', landmark: 'Stadium' },
            'Vyttila': { x: 360, y: 240, name: 'Vyttila', landmark: 'Mobility Hub' },
            'MG Road': { x: 220, y: 230, name: 'MG Road', landmark: 'Commercial' },
            'Ernakulam South': { x: 240, y: 270, name: 'Ernakulam South', landmark: 'Railway' },
            'Thrippunithura': { x: 450, y: 290, name: 'Thrippunithura', landmark: 'Hill Palace' },
            'Fort Kochi': { x: 90, y: 260, name: 'Fort Kochi', landmark: 'Heritage' },
            'Aluva': { x: 370, y: 30, name: 'Aluva', landmark: 'Periyar Metro' },
            'Trivandrum': { x: 300, y: 370, name: 'Trivandrum', landmark: 'Central' },
            'Kozhikode': { x: 180, y: 40, name: 'Kozhikode', landmark: 'Beach Road' }
        };

        this.waypoints = [];
        this.resize();
        window.addEventListener('resize', () => this.resize());
    }

    resize() {
        if (!this.canvas) return;
        const rect = this.canvas.parentElement.getBoundingClientRect();
        this.canvas.width = rect.width;
        this.canvas.height = rect.height || 420;
        this.updateWaypoints();
        this.render();
    }

    setRoute(pickup, destination) {
        this.pickup = pickup || 'Kakkanad';
        this.destination = destination || 'Vyttila';
        this.progress = 0.0;
        this.updateWaypoints();
        this.render();
    }

    updateWaypoints() {
        const p1 = this.locations[this.pickup] || this.locations['Kakkanad'];
        const p2 = this.locations[this.destination] || this.locations['Vyttila'];

        const midX = (p1.x + p2.x) / 2.0;
        const midY = (p1.y + p2.y) / 2.0;
        const jitterX = (p2.y - p1.y) * 0.12;
        const jitterY = (p1.x - p2.x) * 0.12;

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

        const startTime = performance.now();
        const durationMs = durationSeconds * 1000;

        const frame = (now) => {
            const elapsed = now - startTime;
            const t = Math.min(1.0, elapsed / durationMs);
            this.setCarProgress(t);

            if (t < 1.0) {
                this.animationFrame = requestAnimationFrame(frame);
            } else {
                if (onComplete) onComplete();
            }
        };

        this.animationFrame = requestAnimationFrame(frame);
    }

    // Bezier curve point interpolation
    getBezierPoint(p0, p1, p2, t) {
        const oneMinusT = 1.0 - t;
        const x = oneMinusT * oneMinusT * p0.x + 2.0 * oneMinusT * t * p1.x + t * t * p2.x;
        const y = oneMinusT * oneMinusT * p0.y + 2.0 * oneMinusT * t * p1.y + t * t * p2.y;
        
        // Compute tangent for vehicle rotation
        const dx = 2.0 * (oneMinusT) * (p1.x - p0.x) + 2.0 * t * (p2.x - p1.x);
        const dy = 2.0 * (oneMinusT) * (p1.y - p0.y) + 2.0 * t * (p2.y - p1.y);
        const angle = Math.atan2(dy, dx);

        return { x, y, angle };
    }

    render() {
        if (!this.ctx || !this.canvas) return;
        const ctx = this.ctx;
        const w = this.canvas.width;
        const h = this.canvas.height;

        const scaleX = w / 600.0;
        const scaleY = h / 400.0;

        // 1. Dark Slate Tile Background
        ctx.fillStyle = '#090E1A';
        ctx.fillRect(0, 0, w, h);

        // 2. Subtle Grid
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.02)';
        ctx.lineWidth = 1;
        const gridSize = 40;
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

        // 3. Kochi Waterway / Backwaters (Vembanad Lake / Arabian Sea coast)
        ctx.fillStyle = '#062038';
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.bezierCurveTo(70 * scaleX, 100 * scaleY, 40 * scaleX, 220 * scaleY, 0, 400 * scaleY);
        ctx.lineTo(0, 0);
        ctx.fill();

        // Secondary inlet
        ctx.fillStyle = '#072744';
        ctx.beginPath();
        ctx.moveTo(110 * scaleX, 170 * scaleY);
        ctx.bezierCurveTo(150 * scaleX, 210 * scaleY, 190 * scaleX, 280 * scaleY, 170 * scaleX, 350 * scaleY);
        ctx.lineTo(130 * scaleX, 350 * scaleY);
        ctx.bezierCurveTo(140 * scaleX, 270 * scaleY, 110 * scaleX, 210 * scaleY, 80 * scaleX, 180 * scaleY);
        ctx.closePath();
        ctx.fill();

        // 4. City Road Network
        const roads = [
            // NH 66 bypass
            [{ x: 370, y: 30 }, { x: 340, y: 90 }, { x: 330, y: 150 }, { x: 360, y: 240 }, { x: 450, y: 290 }],
            // Seaport-Airport Road
            [{ x: 370, y: 30 }, { x: 460, y: 140 }, { x: 450, y: 290 }],
            // Palarivattom - Kakkanad Link
            [{ x: 330, y: 150 }, { x: 460, y: 140 }],
            // Kaloor - MG Road
            [{ x: 340, y: 90 }, { x: 290, y: 190 }, { x: 220, y: 230 }, { x: 240, y: 270 }],
            // Vyttila - MG Road link
            [{ x: 360, y: 240 }, { x: 240, y: 270 }],
            // Fort Kochi route
            [{ x: 220, y: 230 }, { x: 90, y: 260 }]
        ];

        // Road Outer Glow
        ctx.strokeStyle = '#152238';
        ctx.lineWidth = 6 * Math.min(scaleX, scaleY);
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';
        roads.forEach(r => {
            ctx.beginPath();
            ctx.moveTo(r[0].x * scaleX, r[0].y * scaleY);
            for (let i = 1; i < r.length; i++) {
                ctx.lineTo(r[i].x * scaleX, r[i].y * scaleY);
            }
            ctx.stroke();
        });

        // Road Centerline
        ctx.strokeStyle = '#1F314D';
        ctx.lineWidth = 3 * Math.min(scaleX, scaleY);
        roads.forEach(r => {
            ctx.beginPath();
            ctx.moveTo(r[0].x * scaleX, r[0].y * scaleY);
            for (let i = 1; i < r.length; i++) {
                ctx.lineTo(r[i].x * scaleX, r[i].y * scaleY);
            }
            ctx.stroke();
        });

        // 5. Active Glowing Route Line (if route exists)
        if (this.waypoints.length === 3) {
            const w0 = { x: this.waypoints[0].x * scaleX, y: this.waypoints[0].y * scaleY };
            const w1 = { x: this.waypoints[1].x * scaleX, y: this.waypoints[1].y * scaleY };
            const w2 = { x: this.waypoints[2].x * scaleX, y: this.waypoints[2].y * scaleY };

            // Cyan Path Glow
            ctx.strokeStyle = 'rgba(14, 165, 233, 0.25)';
            ctx.lineWidth = 10 * Math.min(scaleX, scaleY);
            ctx.beginPath();
            ctx.moveTo(w0.x, w0.y);
            ctx.quadraticCurveTo(w1.x, w1.y, w2.x, w2.y);
            ctx.stroke();

            // Core Route Line
            ctx.strokeStyle = '#0284C7';
            ctx.lineWidth = 4 * Math.min(scaleX, scaleY);
            ctx.beginPath();
            ctx.moveTo(w0.x, w0.y);
            ctx.quadraticCurveTo(w1.x, w1.y, w2.x, w2.y);
            ctx.stroke();

            // Animated Traveling Car Marker
            const pos = this.getBezierPoint(w0, w1, w2, this.progress);
            this.drawCarMarker(ctx, pos.x, pos.y, pos.angle);
        }

        // 6. Draw City Landmark Nodes
        Object.values(this.locations).forEach(loc => {
            const lx = loc.x * scaleX;
            const ly = loc.y * scaleY;
            const isPickup = loc.name === this.pickup;
            const isDest = loc.name === this.destination;

            if (isPickup) {
                // Pickup Pin (Green Halo)
                ctx.fillStyle = 'rgba(16, 185, 129, 0.25)';
                ctx.beginPath();
                ctx.arc(lx, ly, 14, 0, Math.PI * 2);
                ctx.fill();

                ctx.fillStyle = '#10B981';
                ctx.beginPath();
                ctx.arc(lx, ly, 6, 0, Math.PI * 2);
                ctx.fill();

                ctx.fillStyle = '#FFFFFF';
                ctx.font = 'bold 11px Plus Jakarta Sans, sans-serif';
                ctx.fillText('● Pickup: ' + loc.name, lx + 12, ly + 4);
            } else if (isDest) {
                // Destination Pin (Blue Halo)
                ctx.fillStyle = 'rgba(14, 165, 233, 0.25)';
                ctx.beginPath();
                ctx.arc(lx, ly, 14, 0, Math.PI * 2);
                ctx.fill();

                ctx.fillStyle = '#0EA5E9';
                ctx.beginPath();
                ctx.arc(lx, ly, 6, 0, Math.PI * 2);
                ctx.fill();

                ctx.fillStyle = '#FFFFFF';
                ctx.font = 'bold 11px Plus Jakarta Sans, sans-serif';
                ctx.fillText('■ Dropoff: ' + loc.name, lx + 12, ly + 4);
            } else {
                // Background Junction Dot
                ctx.fillStyle = '#334155';
                ctx.beginPath();
                ctx.arc(lx, ly, 3.5, 0, Math.PI * 2);
                ctx.fill();

                ctx.fillStyle = '#64748B';
                ctx.font = '9px Plus Jakarta Sans, sans-serif';
                ctx.fillText(loc.name, lx + 7, ly + 3);
            }
        });
    }

    // Draw Sleek Directional Cab Marker
    drawCarMarker(ctx, x, y, angle) {
        ctx.save();
        ctx.translate(x, y);
        ctx.rotate(angle);

        // Vehicle Glow Ring
        ctx.fillStyle = 'rgba(14, 165, 233, 0.35)';
        ctx.beginPath();
        ctx.arc(0, 0, 18, 0, Math.PI * 2);
        ctx.fill();

        // Vehicle Chassis
        ctx.fillStyle = '#0284C7';
        ctx.strokeStyle = '#FFFFFF';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.roundRect(-14, -8, 28, 16, 4);
        ctx.fill();
        ctx.stroke();

        // Windshield
        ctx.fillStyle = '#0B132B';
        ctx.fillRect(-4, -6, 8, 12);

        // Headlights
        ctx.fillStyle = '#FDE047';
        ctx.fillRect(11, -7, 3, 4);
        ctx.fillRect(11, 3, 3, 4);

        ctx.restore();
    }
}
