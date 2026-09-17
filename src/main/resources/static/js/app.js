/**
 * ERRORCab - Commercial Mobility Application Engine
 * Team: ERROR | "Book Smart. Ride Safe."
 * Shared JS Engine, Theme Controller, API Client, Session Manager, and Inline SVG System
 */

const App = {
    // =========================================================================
    // 1. THEME CONTROLLER (LIGHT / DARK / SYSTEM)
    // =========================================================================
    getTheme() {
        return localStorage.getItem('errorcab_theme') || 'system';
    },

    setTheme(theme) {
        if (!['light', 'dark', 'system'].includes(theme)) {
            theme = 'system';
        }
        localStorage.setItem('errorcab_theme', theme);
        this.applyTheme(theme);
        this.showToast('Theme Updated', `Switched to ${theme.toUpperCase()} mode.`, theme === 'dark' ? 'moon' : (theme === 'light' ? 'sun' : 'gear'));
    },

    applyTheme(theme) {
        const root = document.documentElement;
        let resolvedTheme = theme;

        if (theme === 'system') {
            const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
            resolvedTheme = prefersDark ? 'dark' : 'light';
        }

        root.setAttribute('data-theme', resolvedTheme);
        root.setAttribute('data-theme-preference', theme);

        // Update active states on any theme switcher buttons on the page
        document.querySelectorAll('[data-theme-choice]').forEach(btn => {
            const choice = btn.getAttribute('data-theme-choice');
            if (choice === theme) {
                btn.classList.add('active');
                btn.setAttribute('aria-pressed', 'true');
            } else {
                btn.classList.remove('active');
                btn.setAttribute('aria-pressed', 'false');
            }
        });

        // Dispatch event for components that need theme awareness (e.g. Canvas)
        window.dispatchEvent(new CustomEvent('themeChanged', { detail: { theme, resolvedTheme } }));
    },

    initTheme() {
        const currentTheme = this.getTheme();
        this.applyTheme(currentTheme);

        // Automatically populate all theme mount slots across pages
        const mountSelectors = [
            '#themeSelectorSlot',
            '#adminThemeSlot',
            '#driverThemeSlot',
            '#authThemeSlot',
            '#mobileThemeSlot',
            '#themeSelectorMobileSlot',
            '#profileThemeSlot',
            '.theme-mount'
        ];
        mountSelectors.forEach(sel => {
            document.querySelectorAll(sel).forEach(el => {
                el.innerHTML = this.renderThemeSelectorHTML();
            });
        });

        // Listen for OS/browser theme preference changes
        if (window.matchMedia) {
            const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
            mediaQuery.addEventListener('change', (e) => {
                if (this.getTheme() === 'system') {
                    this.applyTheme('system');
                }
            });
        }
    },

    // HTML Generator for Accessible 3-Button Theme Switcher
    renderThemeSelectorHTML(showLabel = true) {
        const current = this.getTheme();
        return `
            <div class="theme-control-wrapper" role="radiogroup" aria-label="Appearance Theme">
                ${showLabel ? `<span class="theme-control-label">Appearance:</span>` : ''}
                <div class="theme-switcher">
                    <button type="button" class="theme-btn ${current === 'light' ? 'active' : ''}" data-theme-choice="light" onclick="App.setTheme('light')" title="Light theme" aria-label="Select Light theme">
                        <span>&#9728; Light</span>
                    </button>
                    <button type="button" class="theme-btn ${current === 'dark' ? 'active' : ''}" data-theme-choice="dark" onclick="App.setTheme('dark')" title="Dark theme" aria-label="Select Dark theme">
                        <span>&#127769; Dark</span>
                    </button>
                    <button type="button" class="theme-btn ${current === 'system' ? 'active' : ''}" data-theme-choice="system" onclick="App.setTheme('system')" title="Follow System theme" aria-label="Select System theme">
                        <span>&#9881; System</span>
                    </button>
                </div>
            </div>
        `;
    },

    // =========================================================================
    // 2. CURRENT USER SESSION & AUTHENTICATION
    // =========================================================================
    getUser() {
        try {
            const data = localStorage.getItem('errorcab_user');
            return data ? JSON.parse(data) : null;
        } catch (e) {
            return null;
        }
    },

    setUser(user) {
        localStorage.setItem('errorcab_user', JSON.stringify(user));
    },

    async logout() {
        try {
            await this.post('/api/auth/logout', {});
        } catch (e) {}
        localStorage.removeItem('errorcab_user');
        window.location.href = '/login.html';
    },

    requireAuth(allowedRoles) {
        let user = this.getUser();
        if (!user) {
            // Provide automatic demo credentials so direct URL inspection works without forced redirect
            if (allowedRoles && allowedRoles.includes('DRIVER')) {
                user = { userId: 2, name: 'Akhil Raj', email: 'driver@example.com', role: 'DRIVER', phone: '+91 98765 43211' };
            } else if (allowedRoles && allowedRoles.includes('ADMIN')) {
                user = { userId: 3, name: 'Operations Hub', email: 'admin@example.com', role: 'ADMIN', phone: '+91 98765 43212' };
            } else {
                user = { userId: 1, name: 'Priya Sharma', email: 'passenger@example.com', role: 'PASSENGER', phone: '+91 98765 43210' };
            }
            this.setUser(user);
            return user;
        }
        if (allowedRoles && !allowedRoles.includes(user.role)) {
            if (allowedRoles.includes('DRIVER')) {
                user = { userId: 2, name: 'Akhil Raj', email: 'driver@example.com', role: 'DRIVER', phone: '+91 98765 43211' };
            } else if (allowedRoles.includes('ADMIN')) {
                user = { userId: 3, name: 'Operations Hub', email: 'admin@example.com', role: 'ADMIN', phone: '+91 98765 43212' };
            } else if (allowedRoles.includes('PASSENGER')) {
                user = { userId: 1, name: 'Priya Sharma', email: 'passenger@example.com', role: 'PASSENGER', phone: '+91 98765 43210' };
            }
            this.setUser(user);
            return user;
        }
        return user;
    },

    // =========================================================================
    // 3. HTTP API CLIENT
    // =========================================================================
    async api(url, options = {}) {
        const defaultHeaders = { 'Content-Type': 'application/json' };
        const user = this.getUser();
        if (user && user.sessionToken) {
            defaultHeaders['Authorization'] = 'Bearer ' + user.sessionToken;
        }
        options.headers = { ...defaultHeaders, ...options.headers };

        try {
            const res = await fetch(url, options);
            if (res.status === 401 && !url.includes('/api/auth/login')) {
                localStorage.removeItem('errorcab_user');
                window.location.href = '/login.html';
                throw new Error('Session expired or unauthorized. Please log in.');
            }
            const data = await res.json().catch(() => ({}));
            if (!res.ok) {
                throw new Error(data.error || 'Request failed with status ' + res.status);
            }
            return data;
        } catch (err) {
            console.error('API Error:', err);
            throw err;
        }
    },

    get(url) {
        return this.api(url, { method: 'GET' });
    },

    post(url, data) {
        return this.api(url, { method: 'POST', body: JSON.stringify(data) });
    },

    put(url, data) {
        return this.api(url, { method: 'PUT', body: JSON.stringify(data) });
    },

    delete(url) {
        return this.api(url, { method: 'DELETE' });
    },

    // =========================================================================
    // 4. FORMATTERS
    // =========================================================================
    formatCurrency(amount) {
        return '₹' + Math.round(amount || 0).toLocaleString('en-IN');
    },

    formatDistance(km) {
        return (Math.round((km || 0) * 10) / 10).toFixed(1) + ' km';
    },

    formatDuration(minutes) {
        const m = Math.round(minutes || 0);
        if (m >= 60) {
            const h = Math.floor(m / 60);
            const rem = m % 60;
            return rem > 0 ? `${h}h ${rem}m` : `${h}h`;
        }
        return m + ' mins';
    },

    formatDate(dateStr) {
        if (!dateStr) return '';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', {
            day: 'numeric',
            month: 'short',
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    // =========================================================================
    // 5. INLINE SVG ICON SYSTEM
    // =========================================================================
    icon(name, size = 18, color = 'currentColor') {
        const icons = {
            car: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"/><circle cx="7" cy="17" r="2"/><path d="M9 17h6"/><circle cx="17" cy="17" r="2"/></svg>`,
            suv: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="10" width="18" height="8" rx="2"/><path d="m5 10 2-4h10l2 4"/><circle cx="7.5" cy="18.5" r="2.5"/><circle cx="16.5" cy="18.5" r="2.5"/></svg>`,
            premium: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`,
            pin: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>`,
            user: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
            users: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
            bell: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>`,
            clock: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`,
            shield: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>`,
            star: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="currentColor" stroke="none"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`,
            check: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>`,
            phone: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>`,
            share: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/></svg>`,
            card: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>`,
            cash: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="2"/></svg>`,
            eye: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>`,
            eyeOff: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>`,
            arrowRight: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>`,
            tag: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>`,
            home: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`,
            search: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>`,
            printer: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 6 2 18 2 18 9"/><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><rect x="6" y="14" width="12" height="8"/></svg>`,
            trash: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>`,
            receipt: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 2v20l2-1 2 1 2-1 2 1 2-1 2 1 2-1 2 1V2l-2 1-2-1-2 1-2-1-2 1-2-1-2 1-2-1Z"/><path d="M16 8h-8"/><path d="M16 12h-8"/><path d="M10 16h-2"/></svg>`,
            power: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18.36 6.64a9 9 0 1 1-12.73 0"/><line x1="12" y1="2" x2="12" y2="12"/></svg>`,
            alertTriangle: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`,
            dashboard: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>`,
            chart: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>`,
            xCircle: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>`,
            checkCircle: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>`,
            menu: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="18" x2="21" y2="18"/></svg>`,
            x: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>`,
            logOut: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>`,
            rupee: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="6" y1="3" x2="18" y2="3"/><line x1="6" y1="8" x2="18" y2="8"/><path d="M6 13l8.5 8"/><path d="M6 13h3a4 4 0 0 0 0-8"/></svg>`,
            sun: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>`,
            moon: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>`,
            monitor: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>`,
            gear: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>`,
            zap: `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>`
        };
        return icons[name] || '';
    },

    // =========================================================================
    // 6. TOAST NOTIFICATION SYSTEM
    // =========================================================================
    showToast(title, message, iconType = 'bell') {
        let container = document.getElementById('toastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toastContainer';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = 'toast';
        const iconSvg = this.icon(iconType, 20, 'var(--primary)') || this.icon('bell', 20, 'var(--primary)');
        toast.innerHTML = `
            <div style="flex-shrink: 0;">${iconSvg}</div>
            <div style="flex-grow: 1;">
                <div style="font-weight: 800; font-size: 13px; line-height: 1.2; color: var(--text-primary);">${title}</div>
                <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">${message}</div>
            </div>
        `;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(40px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    },

    // =========================================================================
    // 7. MODALS
    // =========================================================================
    openModal(id) {
        const modal = document.getElementById(id);
        if (modal) {
            modal.classList.add('open');
            document.body.style.overflow = 'hidden';
        }
    },

    closeModal(id) {
        const modal = document.getElementById(id);
        if (modal) {
            modal.classList.remove('open');
            document.body.style.overflow = '';
        }
    },

    // =========================================================================
    // 8. UTILITIES (PASSWORD, CLIPBOARD, SOS, NOTIFICATIONS)
    // =========================================================================
    togglePassword(inputId, btnId) {
        const input = document.getElementById(inputId);
        const btn = document.getElementById(btnId);
        if (!input) return;

        if (input.type === 'password') {
            input.type = 'text';
            if (btn) btn.innerHTML = this.icon('eyeOff', 18);
        } else {
            input.type = 'password';
            if (btn) btn.innerHTML = this.icon('eye', 18);
        }
    },

    async copyRideDetails(text) {
        if (navigator.clipboard && window.isSecureContext) {
            await navigator.clipboard.writeText(text);
            this.showToast('Copied to Clipboard', 'Ride details link copied.', 'share');
        } else {
            const textArea = document.createElement('textarea');
            textArea.value = text;
            textArea.style.position = 'fixed';
            textArea.style.left = '-999999px';
            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();
            try {
                document.execCommand('copy');
                this.showToast('Copied to Clipboard', 'Ride details link copied.', 'share');
            } catch (err) {
                alert('Ride Details:\n\n' + text);
            }
            textArea.remove();
        }
    },

    triggerSOS() {
        alert("⚠️ EMERGENCY ASSISTANCE DISPATCHED (DEMO)\n\n" +
              "Your live GPS telemetry and vehicle coordinates have been transmitted to:\n" +
              "• Kerala Police Command (112)\n" +
              "• Women Safety Helpline (1091)\n" +
              "• Your Emergency Contacts\n\n" +
              "NOTE: College OOP Demonstration Mode. No live authorities contacted.");
    },

    lastNotificationCount: 0,
    initNotificationPoller(userId) {
        if (!userId) return;
        this.refreshUnreadBadge(userId);
        setInterval(async () => {
            try {
                const notifs = await this.get(`/api/notifications/${userId}`);
                if (notifs && notifs.length > this.lastNotificationCount && this.lastNotificationCount > 0) {
                    const latest = notifs[0];
                    this.showToast(latest.title, latest.message, 'bell');
                }
                this.lastNotificationCount = notifs ? notifs.length : 0;
                this.refreshUnreadBadge(userId);
            } catch (e) {}
        }, 4000);
    },

    async refreshUnreadBadge(userId) {
        if (!userId) {
            const u = this.getUser();
            if (!u) return;
            userId = u.userId;
        }
        try {
            const res = await this.get(`/api/notifications/${userId}/unread-count`);
            const count = res && typeof res.count === 'number' ? res.count : 0;
            document.querySelectorAll('.bell-badge, #unreadBadge').forEach(badge => {
                if (count > 0) {
                    badge.textContent = count > 99 ? '99+' : count;
                    badge.style.display = 'inline-flex';
                } else {
                    badge.style.display = 'none';
                }
            });
        } catch (e) {}
    },

    async toggleNotifications() {
        const modal = document.getElementById('notificationCenterModal');
        if (modal && modal.classList.contains('open')) {
            this.closeModal('notificationCenterModal');
        } else {
            await this.openNotificationCenter();
        }
    },

    async openNotificationCenter() {
        const user = this.getUser();
        if (!user) return;

        let modal = document.getElementById('notificationCenterModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'notificationCenterModal';
            modal.className = 'modal-backdrop';
            modal.innerHTML = `
                <div class="modal-card notification-center-card" style="max-width: 520px; width: 95%;">
                    <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border); padding-bottom: 14px; margin-bottom: 14px;">
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <span style="display: flex; align-items: center; color: var(--primary);">${this.icon('bell', 20)}</span>
                            <h2 style="font-size: 18px; font-weight: 800; margin: 0; color: var(--text-primary);">Notification Center</h2>
                            <span id="notifCenterCountBadge" class="badge badge-primary" style="font-size: 11px;"></span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <button type="button" onclick="App.markAllNotificationsRead(${user.userId})" class="btn btn-outline btn-sm" style="font-size: 11px; padding: 4px 8px;">Mark All Read</button>
                            <button type="button" onclick="App.closeModal('notificationCenterModal')" class="modal-close" style="font-size: 20px; line-height: 1; border: none; background: none; cursor: pointer; color: var(--text-muted);">&times;</button>
                        </div>
                    </div>
                    <div id="notificationCenterList" style="max-height: 380px; overflow-y: auto; padding-right: 4px;">
                        <div style="text-align: center; color: var(--text-muted); padding: 24px;">Loading alerts...</div>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
        }

        this.openModal('notificationCenterModal');
        await this.renderNotificationList(user.userId);
    },

    async renderNotificationList(userId) {
        const listEl = document.getElementById('notificationCenterList');
        const badgeEl = document.getElementById('notifCenterCountBadge');
        if (!listEl) return;

        try {
            const notifs = await this.get(`/api/notifications/${userId}`);
            const unreadList = notifs.filter(n => !n.isRead);
            if (badgeEl) {
                badgeEl.textContent = `${unreadList.length} Unread`;
                badgeEl.style.display = unreadList.length > 0 ? 'inline-block' : 'none';
            }

            if (!notifs || notifs.length === 0) {
                listEl.innerHTML = `
                    <div style="text-align: center; color: var(--text-muted); padding: 36px 16px;">
                        <div style="font-size: 36px; margin-bottom: 8px;">🔕</div>
                        <div style="font-weight: 700; color: var(--text-primary);">No notifications yet</div>
                        <div style="font-size: 12px; margin-top: 4px;">Ride updates, driver dispatches, and alerts will appear here.</div>
                    </div>
                `;
                return;
            }

            listEl.innerHTML = notifs.map(n => {
                const isUnread = !n.isRead;
                const timeStr = this.formatDate(n.timestamp);
                return `
                    <div class="notification-item ${isUnread ? 'unread' : ''}" style="display: flex; gap: 12px; padding: 12px; border-radius: var(--radius-md); margin-bottom: 8px; background: ${isUnread ? 'var(--bg-card-subtle)' : 'transparent'}; border: 1px solid ${isUnread ? 'var(--primary-subtle)' : 'var(--border)'};">
                        <div style="width: 8px; height: 8px; border-radius: 50%; background: ${isUnread ? 'var(--primary)' : 'transparent'}; margin-top: 6px; flex-shrink: 0;"></div>
                        <div style="flex-grow: 1;">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 8px;">
                                <div style="font-weight: 800; font-size: 13px; color: var(--text-primary);">${n.title}</div>
                                <span style="font-size: 11px; color: var(--text-muted); white-space: nowrap;">${timeStr}</span>
                            </div>
                            <div style="font-size: 12px; color: var(--text-secondary); margin-top: 3px; line-height: 1.4;">${n.message}</div>
                            ${isUnread ? `
                                <div style="margin-top: 6px; text-align: right;">
                                    <button type="button" onclick="App.markNotificationRead(${n.id}, ${userId})" class="btn btn-secondary btn-sm" style="font-size: 10px; padding: 2px 8px; font-weight: 700;">Mark Read</button>
                                </div>
                            ` : ''}
                        </div>
                    </div>
                `;
            }).join('');
        } catch (e) {
            listEl.innerHTML = `<div style="color: var(--danger); text-align: center; padding: 20px;">Failed to load notifications.</div>`;
        }
    },

    async markNotificationRead(id, userId) {
        try {
            await this.put(`/api/notifications/${id}/read`, {});
            await this.renderNotificationList(userId);
            this.refreshUnreadBadge(userId);
        } catch (e) {}
    },

    async markAllNotificationsRead(userId) {
        try {
            await this.put(`/api/notifications/user/${userId}/read-all`, {});
            await this.renderNotificationList(userId);
            this.refreshUnreadBadge(userId);
        } catch (e) {}
    }
};

// Automatically initialize theme listener on DOM ready
if (typeof window !== 'undefined') {
    window.addEventListener('DOMContentLoaded', () => {
        App.initTheme();
    });
}
