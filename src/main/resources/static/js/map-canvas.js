/**
 * ERRORCab - Commercial Simulated GPS Map & Telemetry Canvas Engine
 * India-Wide Dynamic Cartography & Navigation Visualizer
 * Dynamic Geographic Projection: (lat, lon) -> Bounding Box -> Conformal Aspect Projection -> Canvas (x, y)
 * Supports all Indian cities, interstate corridors, and local urban zones.
 * Works completely offline with zero external paid map APIs.
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

        // Authoritative Coordinates Database across India (cities, hubs, airports, landmarks)
        this.allLocations = {
            // National Capital Region (Delhi / Agra / NCR)
            'delhi': { lat: 28.6139, lon: 77.2090, name: 'Delhi', region: 'National Capital' },
            'newdelhi': { lat: 28.6139, lon: 77.2090, name: 'New Delhi', region: 'National Capital' },
            'delhiairport': { lat: 28.5562, lon: 77.1000, name: 'Delhi Airport (T3)', region: 'Delhi NCR' },
            'delhiairportt3': { lat: 28.5562, lon: 77.1000, name: 'Delhi Airport (T3)', region: 'Delhi NCR' },
            'indiagate': { lat: 28.6129, lon: 77.2295, name: 'India Gate', region: 'Delhi' },
            'redfort': { lat: 28.6562, lon: 77.2410, name: 'Red Fort', region: 'Old Delhi' },
            'qutubminar': { lat: 28.5245, lon: 77.1855, name: 'Qutub Minar', region: 'South Delhi' },
            'connaughtplace': { lat: 28.6315, lon: 77.2167, name: 'Connaught Place', region: 'Central Delhi' },
            'noida': { lat: 28.5355, lon: 77.3910, name: 'Noida', region: 'Uttar Pradesh' },
            'gurugram': { lat: 28.4595, lon: 77.0266, name: 'Gurugram', region: 'Haryana' },
            'agra': { lat: 27.1767, lon: 78.0081, name: 'Agra', region: 'Uttar Pradesh' },
            'tajmahal': { lat: 27.1751, lon: 78.0421, name: 'Taj Mahal', region: 'Agra' },
            'mathura': { lat: 27.4924, lon: 77.6737, name: 'Mathura', region: 'Uttar Pradesh' },

            // Western India (Mumbai / Pune / Goa)
            'mumbai': { lat: 18.9220, lon: 72.8347, name: 'Mumbai', region: 'Maharashtra' },
            'gatewayofindia': { lat: 18.9220, lon: 72.8347, name: 'Gateway of India', region: 'South Mumbai' },
            'mumbaiairport': { lat: 19.0896, lon: 72.8656, name: 'Mumbai Airport (T2)', region: 'Mumbai' },
            'bandra': { lat: 19.0596, lon: 72.8295, name: 'Bandra', region: 'Mumbai' },
            'marinedrive': { lat: 18.9432, lon: 72.8230, name: 'Marine Drive', region: 'Mumbai' },
            'pune': { lat: 18.5204, lon: 73.8567, name: 'Pune', region: 'Maharashtra' },
            'goa': { lat: 15.4909, lon: 73.8278, name: 'Goa', region: 'Goa' },

            // Southern India (Bengaluru / Chennai / Hyderabad)
            'bengaluru': { lat: 12.9716, lon: 77.5946, name: 'Bengaluru', region: 'Karnataka' },
            'bangalore': { lat: 12.9716, lon: 77.5946, name: 'Bengaluru', region: 'Karnataka' },
            'chennai': { lat: 13.0827, lon: 80.2707, name: 'Chennai', region: 'Tamil Nadu' },
            'hyderabad': { lat: 17.3850, lon: 78.4867, name: 'Hyderabad', region: 'Telangana' },
            'mysuru': { lat: 12.2958, lon: 76.6394, name: 'Mysuru', region: 'Karnataka' },
            'coimbatore': { lat: 11.0168, lon: 76.9558, name: 'Coimbatore', region: 'Tamil Nadu' },

            // Kerala Corridor & Destinations
            'kochi': { lat: 9.9658, lon: 76.2421, name: 'Kochi', region: 'Kerala' },
            'fortkochi': { lat: 9.9658, lon: 76.2421, name: 'Fort Kochi', region: 'Kochi' },
            'kakkanad': { lat: 10.0159, lon: 76.3419, name: 'Kakkanad', region: 'Infopark IT Hub' },
            'edappally': { lat: 10.0261, lon: 76.3125, name: 'Edappally', region: 'Lulu & Metro' },
            'palarivattom': { lat: 10.0034, lon: 76.3075, name: 'Palarivattom', region: 'Civil Line' },
            'kaloor': { lat: 9.9932, lon: 76.2934, name: 'Kaloor', region: 'JLN Stadium' },
            'vyttila': { lat: 9.9678, lon: 76.3184, name: 'Vyttila', region: 'Mobility Hub' },
            'mgroad': { lat: 9.9723, lon: 76.2825, name: 'MG Road', region: 'Commercial Blvd' },
            'ernakulamsouth': { lat: 9.9654, lon: 76.2891, name: 'Ernakulam South', region: 'Central Junction' },
            'thrippunithura': { lat: 9.9515, lon: 76.3508, name: 'Thrippunithura', region: 'Hill Palace' },
            'aluva': { lat: 10.1076, lon: 76.3516, name: 'Aluva', region: 'Periyar River' },
            'munnar': { lat: 10.0889, lon: 77.0595, name: 'Munnar', region: 'Idukki Tea Hills' },
            'kozhikode': { lat: 11.2588, lon: 75.7804, name: 'Kozhikode', region: 'Malabar Coast' },
            'calicut': { lat: 11.2588, lon: 75.7804, name: 'Kozhikode', region: 'Malabar Coast' },
            'perinthalmanna': { lat: 10.9760, lon: 76.2254, name: 'Perinthalmanna', region: 'Valluvanad' },
            'thrissur': { lat: 10.5276, lon: 76.2144, name: 'Thrissur', region: 'Cultural Capital' },
            'alappuzha': { lat: 9.4981, lon: 76.3388, name: 'Alappuzha', region: 'Backwaters' },
            'kottayam': { lat: 9.5916, lon: 76.5222, name: 'Kottayam', region: 'Central Kerala' },
            'trivandrum': { lat: 8.5241, lon: 76.9366, name: 'Trivandrum', region: 'Technopark' },
            'thiruvananthapuram': { lat: 8.5241, lon: 76.9366, name: 'Trivandrum', region: 'Technopark' },
            'wayanad': { lat: 11.6103, lon: 76.0827, name: 'Wayanad', region: 'Highland Rainforest' },
            'vagamon': { lat: 9.6869, lon: 76.9056, name: 'Vagamon', region: 'Pine Hills' },
            'thekkady': { lat: 9.6031, lon: 77.1615, name: 'Thekkady', region: 'Periyar Reserve' },

            // Eastern & Northern India
            'kolkata': { lat: 22.5726, lon: 88.3639, name: 'Kolkata', region: 'West Bengal' },
            'jaipur': { lat: 26.9124, lon: 75.7873, name: 'Jaipur', region: 'Pink City' },
            'amritsar': { lat: 31.6340, lon: 74.8723, name: 'Amritsar', region: 'Punjab' },
            'varanasi': { lat: 25.3176, lon: 82.9739, name: 'Varanasi', region: 'Uttar Pradesh' }
        };

        this.pickupCoords = { lat: 10.0159, lon: 76.3419 }; // Kakkanad
        this.destCoords = { lat: 9.9678, lon: 76.3184 };   // Vyttila
        this.bounds = null;
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
            this.dashOffset = (timestamp / 45) % 24;   // Route flowing dash offset

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
        const displayWidth = Math.max(280, rect.width || 600);
        const displayHeight = Math.max(200, rect.height || 420);

        this.canvas.width = displayWidth * dpr;
        this.canvas.height = displayHeight * dpr;
        this.canvas.style.width = displayWidth + 'px';
        this.canvas.style.height = displayHeight + 'px';

        if (this.ctx) {
            this.ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        }

        this.width = displayWidth;
        this.height = displayHeight;

        this.updateBoundingBox();
        this.updateWaypoints();
        this.render();
    }

    /**
     * Resolves coordinates from given argument (object or destination name).
     */
    resolveCoords(nameOrCoords) {
        if (!nameOrCoords) return { lat: 10.0159, lon: 76.3419 };
        if (typeof nameOrCoords === 'object') {
            const lat = Number(nameOrCoords.lat || nameOrCoords.latitude || 0);
            const lon = Number(nameOrCoords.lon || nameOrCoords.lng || nameOrCoords.longitude || 0);
            if (lat !== 0 && lon !== 0) {
                return { lat, lon };
            }
        }

        const raw = String(nameOrCoords).trim();
        const key = raw.toLowerCase().replace(/[^a-z0-9]/g, '');

        if (this.allLocations[key]) {
            return { lat: this.allLocations[key].lat, lon: this.allLocations[key].lon };
        }

        // Substring / fuzzy match across registered Indian hubs
        for (const [k, loc] of Object.entries(this.allLocations)) {
            if (key === k || key.startsWith(k) || k.startsWith(key) || key.includes(k) || k.includes(key)) {
                return { lat: loc.lat, lon: loc.lon };
            }
        }

        // Asynchronous resolution fallback via server endpoint if available
        if (typeof fetch === 'function' && !this.resolving) {
            this.resolving = true;
            fetch(`/api/map/resolve?query=${encodeURIComponent(raw)}`)
                .then(r => r.json())
                .then(data => {
                    if (data && data.resolved && data.latitude && data.longitude) {
                        this.allLocations[key] = { lat: data.latitude, lon: data.longitude, name: raw };
                        this.updateBoundingBox();
                        this.updateWaypoints();
                        this.render();
                    }
                })
                .catch(() => {})
                .finally(() => { this.resolving = false; });
        }

        // Fallback to central Kochi coordinates if completely unknown
        return { lat: 10.0159, lon: 76.3419 };
    }

    /**
     * Sets the active route, computes dynamic geographic bounding box,
     * fits both locations inside Canvas with padding, and animates the trajectory.
     */
    setRoute(pickup, destination, distanceKm = null, durationMins = null, pickupCoords = null, destCoords = null) {
        this.pickup = pickup || 'Kakkanad';
        this.destination = destination || 'Vyttila';
        this.progress = 0.0;

        if (distanceKm !== null && distanceKm !== undefined) {
            this.distanceText = (Math.round(distanceKm * 10) / 10).toFixed(1) + ' km';
        }
        if (durationMins !== null && durationMins !== undefined) {
            this.durationText = Math.round(durationMins) + ' min';
        }

        this.pickupCoords = this.resolveCoords(pickupCoords || pickup);
        this.destCoords = this.resolveCoords(destCoords || destination);

        this.updateBoundingBox();
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

    /**
     * Calculates the dynamic geographic bounding box with padding and conformal aspect ratio correction.
     */
    updateBoundingBox() {
        const p1 = this.pickupCoords || { lat: 10.0159, lon: 76.3419 };
        const p2 = this.destCoords || { lat: 9.9678, lon: 76.3184 };

        let minLat = Math.min(p1.lat, p2.lat);
        let maxLat = Math.max(p1.lat, p2.lat);
        let minLon = Math.min(p1.lon, p2.lon);
        let maxLon = Math.max(p1.lon, p2.lon);

        // Ensure minimum geographic span (approx 6-8 km) so city-scale points do not collapse
        const minSpan = 0.055;
        if (maxLat - minLat < minSpan) {
            const mid = (minLat + maxLat) / 2.0;
            minLat = mid - minSpan / 2.0;
            maxLat = mid + minSpan / 2.0;
        }
        if (maxLon - minLon < minSpan) {
            const mid = (minLon + maxLon) / 2.0;
            minLon = mid - minSpan / 2.0;
            maxLon = mid + minSpan / 2.0;
        }

        // Generous margin padding (26%) so markers & badges fit comfortably inside canvas
        const padRatio = 0.26;
        const latSpan = maxLat - minLat;
        const lonSpan = maxLon - minLon;
        let bMinLat = minLat - latSpan * padRatio;
        let bMaxLat = maxLat + latSpan * padRatio;
        let bMinLon = minLon - lonSpan * padRatio;
        let bMaxLon = maxLon + lonSpan * padRatio;

        // Conformal aspect ratio correction to match Canvas dimensions
        const w = this.width || 600;
        const h = this.height || 420;
        const padCanvas = 40;
        const usableW = Math.max(120, w - padCanvas * 2);
        const usableH = Math.max(100, h - padCanvas * 2);

        const midLat = (bMinLat + bMaxLat) / 2.0;
        const cosLat = Math.max(0.15, Math.cos((midLat * Math.PI) / 180));

        const geoAspect = ((bMaxLon - bMinLon) * cosLat) / (bMaxLat - bMinLat);
        const canvasAspect = usableW / usableH;

        if (geoAspect > canvasAspect) {
            // Geographically wider than canvas: expand latitude range vertically
            const targetLatSpan = ((bMaxLon - bMinLon) * cosLat) / canvasAspect;
            const diff = (targetLatSpan - (bMaxLat - bMinLat)) / 2.0;
            bMinLat -= diff;
            bMaxLat += diff;
        } else {
            // Geographically taller than canvas: expand longitude range horizontally
            const targetLonSpan = ((bMaxLat - bMinLat) * canvasAspect) / cosLat;
            const diff = (targetLonSpan - (bMaxLon - bMinLon)) / 2.0;
            bMinLon -= diff;
            bMaxLon += diff;
        }

        this.bounds = {
            minLat: bMinLat,
            maxLat: bMaxLat,
            minLon: bMinLon,
            maxLon: bMaxLon,
            usableW,
            usableH,
            padX: padCanvas,
            padY: padCanvas
        };
    }

    /**
     * Projects any geographic (lat, lon) to Canvas coordinate (x, y)
     * using the current dynamic viewport bounding box.
     */
    project(lat, lon) {
        if (!this.bounds) this.updateBoundingBox();
        const b = this.bounds;
        const x = b.padX + ((lon - b.minLon) / (b.maxLon - b.minLon)) * b.usableW;
        // Canvas y=0 is at top, increasing downwards; latitude increases upwards (North)
        const y = b.padY + ((b.maxLat - lat) / (b.maxLat - b.minLat)) * b.usableH;
        return { x, y };
    }

    updateWaypoints() {
        this.updateBoundingBox();
        const p1 = this.pickupCoords || { lat: 10.0159, lon: 76.3419 };
        const p2 = this.destCoords || { lat: 9.9678, lon: 76.3184 };

        const pt0 = this.project(p1.lat, p1.lon);
        const pt2 = this.project(p2.lat, p2.lon);

        const midX = (pt0.x + pt2.x) / 2.0;
        const midY = (pt0.y + pt2.y) / 2.0;
        const dx = pt2.x - pt0.x;
        const dy = pt2.y - pt0.y;
        const dist = Math.sqrt(dx * dx + dy * dy);

        let pt1;
        if (dist < 8) {
            pt1 = { x: midX, y: midY };
        } else {
            // Perpendicular normal vector for natural road curve
            const nx = -dy / dist;
            const ny = dx / dist;
            const curveOffset = Math.min(42, Math.max(14, dist * 0.13));
            pt1 = { x: midX + nx * curveOffset, y: midY + ny * curveOffset };
        }

        this.waypoints = [pt0, pt1, pt2];
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

    // Quadratic Bezier curve point interpolation with tangent rotation
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
        const minScale = Math.min(w / 600.0, h / 400.0);

        ctx.clearRect(0, 0, w, h);

        // 1. Navigation Surface Canvas
        ctx.fillStyle = isDark ? '#0B1120' : '#F8FAFC';
        ctx.fillRect(0, 0, w, h);

        // 2. Subtle Cartography Coordinate Grid
        ctx.strokeStyle = isDark ? 'rgba(255, 255, 255, 0.03)' : 'rgba(15, 23, 42, 0.04)';
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

        if (!this.bounds) this.updateBoundingBox();
        const b = this.bounds;

        // 3. Conditional Regional Coastal Waterways (ONLY rendered when viewport is focused on Kochi)
        const isKochiFocused = (b.minLat >= 9.6 && b.maxLat <= 10.3 && b.minLon >= 76.0 && b.maxLon <= 76.6);
        if (isKochiFocused) {
            ctx.fillStyle = isDark ? '#081D33' : '#E0F2FE';
            ctx.beginPath();
            const sea1 = this.project(10.15, 76.15);
            const sea2 = this.project(9.85, 76.18);
            ctx.moveTo(0, 0);
            ctx.lineTo(sea1.x, 0);
            ctx.bezierCurveTo(sea1.x + 20, (sea1.y + sea2.y) / 2, sea2.x + 20, sea2.y, 0, h);
            ctx.closePath();
            ctx.fill();

            ctx.strokeStyle = isDark ? 'rgba(56, 189, 248, 0.18)' : 'rgba(2, 132, 199, 0.2)';
            ctx.lineWidth = 1.5;
            ctx.stroke();
        }

        // 4. Regional Highway Network / Transit Corridors between in-bounds locations
        const inBoundsLocations = Object.values(this.allLocations).filter(loc => {
            return loc.lat >= b.minLat && loc.lat <= b.maxLat && loc.lon >= b.minLon && loc.lon <= b.maxLon;
        });

        // Draw connecting secondary road links if 2+ regional hubs are visible
        if (inBoundsLocations.length >= 2) {
            ctx.strokeStyle = isDark ? '#1E293B' : '#CBD5E1';
            ctx.lineWidth = 2 * minScale;
            ctx.setLineDash([3 * minScale, 6 * minScale]);
            for (let i = 0; i < Math.min(inBoundsLocations.length, 5); i++) {
                for (let j = i + 1; j < Math.min(inBoundsLocations.length, 5); j++) {
                    const l1 = inBoundsLocations[i];
                    const l2 = inBoundsLocations[j];
                    const pA = this.project(l1.lat, l1.lon);
                    const pB = this.project(l2.lat, l2.lon);
                    const d = Math.hypot(pB.x - pA.x, pB.y - pA.y);
                    if (d < w * 0.7) {
                        ctx.beginPath();
                        ctx.moveTo(pA.x, pA.y);
                        ctx.lineTo(pB.x, pB.y);
                        ctx.stroke();
                    }
                }
            }
            ctx.setLineDash([]);
        }

        // 5. Active Trip Route (Vibrant Flowing Gradient Line)
        if (this.waypoints && this.waypoints.length === 3) {
            const w0 = this.waypoints[0];
            const w1 = this.waypoints[1];
            const w2 = this.waypoints[2];

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

        // 6. In-Bounds Regional Landmarks / Enroute Hubs (with collision avoidance)
        const ptPickup = this.project(this.pickupCoords.lat, this.pickupCoords.lon);
        const ptDest = this.project(this.destCoords.lat, this.destCoords.lon);
        const labeledPoints = [
            { x: ptPickup.x, y: ptPickup.y, r: 52 }, // Clear zone around pickup badge
            { x: ptDest.x, y: ptDest.y, r: 52 }      // Clear zone around destination badge
        ];

        inBoundsLocations.forEach(loc => {
            const isPickup = (loc.name.toLowerCase() === (this.pickup || '').toLowerCase());
            const isDest = (loc.name.toLowerCase() === (this.destination || '').toLowerCase());
            if (isPickup || isDest) return; // Highlighted separately

            const pt = this.project(loc.lat, loc.lon);
            const collides = labeledPoints.some(lp => Math.hypot(lp.x - pt.x, lp.y - pt.y) < lp.r);
            if (collides) return;

            labeledPoints.push({ x: pt.x, y: pt.y, r: 32 });

            ctx.fillStyle = isDark ? '#334155' : '#94A3B8';
            ctx.beginPath();
            ctx.arc(pt.x, pt.y, 2.5 * minScale, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = isDark ? '#64748B' : '#94A3B8';
            ctx.font = `500 ${Math.max(8.5, 9 * minScale)}px 'Plus Jakarta Sans', sans-serif`;
            ctx.fillText(loc.name, pt.x + 6 * minScale, pt.y + 3 * minScale);
        });

        // 7. Prominent Pickup & Destination Location Pins

        // Pickup Marker (Emerald Green Pulsing Beacon)
        const pulseRadius1 = 12 * minScale + (this.pulseTime * 8 * minScale);
        const pulseAlpha1 = Math.max(0, 0.4 - (this.pulseTime * 0.13));

        ctx.fillStyle = `rgba(16, 185, 129, ${pulseAlpha1})`;
        ctx.beginPath();
        ctx.arc(ptPickup.x, ptPickup.y, pulseRadius1, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = 'rgba(16, 185, 129, 0.35)';
        ctx.beginPath();
        ctx.arc(ptPickup.x, ptPickup.y, 11 * minScale, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = '#10B981';
        ctx.strokeStyle = '#FFFFFF';
        ctx.lineWidth = 2 * minScale;
        ctx.beginPath();
        ctx.arc(ptPickup.x, ptPickup.y, 6 * minScale, 0, Math.PI * 2);
        ctx.fill();
        ctx.stroke();

        this.drawPillBadge(ctx, ptPickup.x, ptPickup.y - 18 * minScale, `Pickup: ${this.pickup}`, '#10B981', isDark, minScale);

        // Destination Marker (Cyan / Deep Blue Pulsing Beacon)
        const destPulse = (this.pulseTime + 1.5) % 3.0;
        const pulseRadius2 = 12 * minScale + (destPulse * 8 * minScale);
        const pulseAlpha2 = Math.max(0, 0.35 - (destPulse * 0.12));

        ctx.fillStyle = `rgba(14, 165, 233, ${pulseAlpha2})`;
        ctx.beginPath();
        ctx.arc(ptDest.x, ptDest.y, pulseRadius2, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = 'rgba(14, 165, 233, 0.35)';
        ctx.beginPath();
        ctx.arc(ptDest.x, ptDest.y, 11 * minScale, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = '#0284C7';
        ctx.strokeStyle = '#FFFFFF';
        ctx.lineWidth = 2 * minScale;
        ctx.beginPath();
        ctx.roundRect(ptDest.x - 5 * minScale, ptDest.y - 5 * minScale, 10 * minScale, 10 * minScale, 2 * minScale);
        ctx.fill();
        ctx.stroke();

        this.drawPillBadge(ctx, ptDest.x, ptDest.y - 18 * minScale, `Destination: ${this.destination}`, '#0284C7', isDark, minScale);

        // 8. In-Map Telemetry HUD Overlay
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
        const boxX = Math.max(6, Math.min(this.width - boxW - 6, x - boxW / 2));
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
        const hudW = Math.min(280 * minScale, w - 28 * minScale);
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
        ctx.fillText('SIMULATED GPS • INDIA MOBILITY NETWORK', hudX + 22 * minScale, hudY + 17 * minScale);

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
        ctx.fillRect(2 * minScale, -7 * minScale, 6 * minScale, 14 * minScale);
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
