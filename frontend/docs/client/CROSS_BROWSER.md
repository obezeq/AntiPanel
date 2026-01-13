# Cross-Browser Compatibility - AntiPanel Frontend

## Overview

AntiPanel frontend is built with Angular 21, which targets modern browsers by default. This document describes browser compatibility, testing methodology, and any polyfills or workarounds applied.

## Target Browsers

Angular 21 uses the following default browser targets (via browserslist):

| Browser | Minimum Version | Support Level |
|---------|-----------------|---------------|
| Chrome | Last 2 versions | Full |
| Firefox | Last 2 versions | Full |
| Edge | Last 2 versions | Full |
| Safari | Last 2 versions | Full |
| iOS Safari | Last 2 versions | Full |
| Chrome Android | Last 2 versions | Full |
| Firefox Android | Last 2 versions | Full |

### Explicitly NOT Supported

- Internet Explorer 11 (deprecated, no longer supported by Angular)
- Legacy Edge (EdgeHTML, pre-Chromium)
- Browsers older than 2 major versions

## Features Used and Browser Support

### JavaScript/TypeScript Features

| Feature | Chrome | Firefox | Safari | Edge |
|---------|--------|---------|--------|------|
| ES2022+ (async/await, optional chaining) | 91+ | 89+ | 15+ | 91+ |
| Private class fields (`#field`) | 74+ | 90+ | 14.1+ | 79+ |
| `crypto.randomUUID()` | 92+ | 95+ | 15.4+ | 92+ |
| `structuredClone()` | 98+ | 94+ | 15.4+ | 98+ |
| Nullish coalescing (`??`) | 80+ | 72+ | 13.1+ | 80+ |
| Optional chaining (`?.`) | 80+ | 74+ | 13.1+ | 80+ |

### CSS Features

| Feature | Chrome | Firefox | Safari | Edge |
|---------|--------|---------|--------|------|
| CSS Custom Properties | 49+ | 31+ | 9.1+ | 15+ |
| CSS Grid | 57+ | 52+ | 10.1+ | 16+ |
| Flexbox | 29+ | 28+ | 9+ | 12+ |
| `gap` in Flexbox | 84+ | 63+ | 14.1+ | 84+ |
| `:has()` selector | 105+ | 121+ | 15.4+ | 105+ |
| Container queries | 105+ | 110+ | 16+ | 105+ |
| CSS Nesting | 120+ | 117+ | 17.2+ | 120+ |

### Web APIs

| API | Chrome | Firefox | Safari | Edge | Usage in App |
|-----|--------|---------|--------|------|--------------|
| Fetch API | 42+ | 39+ | 10.1+ | 14+ | HTTP requests |
| localStorage | 4+ | 3.5+ | 4+ | 12+ | Token storage |
| IntersectionObserver | 51+ | 55+ | 12.1+ | 15+ | Lazy loading |
| ResizeObserver | 64+ | 69+ | 13.1+ | 79+ | Responsive components |
| Clipboard API | 66+ | 63+ | 13.1+ | 79+ | Copy functionality |

## Testing Methodology

### Browsers Tested

| Browser | Version | Platform | Status |
|---------|---------|----------|--------|
| Chrome | 120+ | Windows/macOS/Linux | Tested |
| Firefox | 121+ | Windows/macOS/Linux | Tested |
| Safari | 17+ | macOS | Tested |
| Edge | 120+ | Windows | Tested |
| Chrome Mobile | 120+ | Android | Tested |
| Safari Mobile | 17+ | iOS | Tested |

### Testing Checklist

- [x] All routes navigate correctly
- [x] Forms submit and validate
- [x] Authentication flow works
- [x] Orders list with pagination
- [x] Wallet deposits initiate
- [x] Loading states display
- [x] Error states display
- [x] Responsive layout adapts
- [x] Dark/light theme toggles
- [x] Icons render correctly

### Automated Testing

```bash
# Run unit tests with Vitest
bun run test

# Run tests with coverage
bun run test:coverage
```

Tests run in a Node.js environment with jsdom, which simulates browser APIs.

## Known Issues and Workarounds

### 1. Safari Input Focus

**Issue:** Safari may not respect `autofocus` on dynamically inserted inputs.

**Workaround:** Programmatically focus after view init:

```typescript
ngAfterViewInit(): void {
  setTimeout(() => this.inputRef?.nativeElement?.focus(), 0);
}
```

### 2. Firefox Scrollbar Styling

**Issue:** Firefox uses different scrollbar styling API.

**Solution:** Use both standards:

```scss
// Webkit browsers
::-webkit-scrollbar { ... }

// Firefox
scrollbar-width: thin;
scrollbar-color: var(--color-border) transparent;
```

### 3. iOS Safari 100vh Issue

**Issue:** `100vh` on iOS Safari includes browser chrome.

**Workaround:** Use `100dvh` (dynamic viewport height):

```scss
.full-height {
  min-height: 100vh;
  min-height: 100dvh; // Modern fallback
}
```

### 4. Date Input Formatting

**Issue:** Date inputs render differently across browsers.

**Solution:** Use Angular reactive forms with manual date formatting:

```typescript
// Format date for input
formatDateForInput(date: Date): string {
  return date.toISOString().split('T')[0];
}
```

## Polyfills

Angular 21 includes minimal polyfills by default. The following are NOT needed for modern browser targets:

| Polyfill | Needed? | Reason |
|----------|---------|--------|
| zone.js | Optional | Can use zoneless with signals |
| core-js | No | ES2022 supported natively |
| fetch | No | Supported in all targets |
| Promise | No | Supported in all targets |
| Array methods | No | Supported in all targets |

### polyfills.ts Configuration

```typescript
// src/polyfills.ts
// Only zone.js is included by default
import 'zone.js';

// No additional polyfills needed for modern browsers
```

## Performance Targets

### Lighthouse Scores (Target: 80+)

| Metric | Target | Description |
|--------|--------|-------------|
| Performance | 80+ | Load time, interactivity |
| Accessibility | 90+ | ARIA, contrast, labels |
| Best Practices | 90+ | Security, modern APIs |
| SEO | 80+ | Meta tags, structure |

### Core Web Vitals

| Metric | Target | Description |
|--------|--------|-------------|
| LCP (Largest Contentful Paint) | < 2.5s | Main content load |
| FID (First Input Delay) | < 100ms | Interactivity |
| CLS (Cumulative Layout Shift) | < 0.1 | Visual stability |

## Build Configuration

### Production Build

```bash
# Build for production with optimizations
ng build --configuration production
```

Includes:
- Tree shaking
- Code splitting
- Minification
- Differential loading (if needed)
- Source maps (disabled)

### Browser Targets Override (if needed)

Create `.browserslistrc` in project root:

```
# Supported browsers
last 2 Chrome versions
last 2 Firefox versions
last 2 Safari versions
last 2 Edge versions
not dead
```

## Accessibility Testing

Cross-browser accessibility features tested:

- [x] Screen reader compatibility (NVDA, VoiceOver)
- [x] Keyboard navigation
- [x] Focus indicators visible
- [x] Color contrast ratios (WCAG AA)
- [x] ARIA labels on interactive elements
- [x] Skip links for main content

## Responsive Design

Breakpoints tested across browsers:

| Breakpoint | Width | Target Devices |
|------------|-------|----------------|
| Mobile | < 640px | Phones |
| Tablet | 640px - 1024px | Tablets, small laptops |
| Desktop | > 1024px | Laptops, desktops |

```scss
// Breakpoint usage
@media (min-width: 640px) { /* Tablet+ */ }
@media (min-width: 1024px) { /* Desktop */ }
```

## Recommendations

1. **Keep browsers updated**: Test with latest stable versions
2. **Monitor Can I Use**: Check feature support before using new APIs
3. **Graceful degradation**: Provide fallbacks for non-critical features
4. **Progressive enhancement**: Core functionality works everywhere
5. **Regular testing**: Include cross-browser testing in CI/CD
