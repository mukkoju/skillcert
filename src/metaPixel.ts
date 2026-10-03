type PixelParameters = Record<string, string | number | boolean | undefined>

declare global {
  interface Window {
    fbq?: ((action: string, event: string, parameters?: PixelParameters) => void) & {
      callMethod?: (...args: unknown[]) => void
      queue?: unknown[]
      loaded?: boolean
      version?: string
    }
    _fbq?: Window['fbq']
  }
}

// A Pixel ID is public by design. The environment variable permits a distinct
// staging Pixel later; the production ID below makes the current setup active.
const pixelId = import.meta.env.VITE_META_PIXEL_ID ?? '2992328727773760'

export function initialiseMetaPixel() {
  if (typeof window === 'undefined' || window.fbq) return

  const fbq = function (...args: unknown[]) {
    if (fbq.callMethod) fbq.callMethod.apply(fbq, args)
    else (fbq.queue ??= []).push(args)
  } as NonNullable<Window['fbq']>
  fbq.loaded = true
  fbq.version = '2.0'
  window.fbq = fbq
  window._fbq = fbq

  const script = document.createElement('script')
  script.async = true
  script.src = 'https://connect.facebook.net/en_US/fbevents.js'
  document.head.appendChild(script)
  window.fbq('init', pixelId)
}

export function trackPixel(event: string, parameters?: PixelParameters) {
  window.fbq?.('track', event, parameters)
}
