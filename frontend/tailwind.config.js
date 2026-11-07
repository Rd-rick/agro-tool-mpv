/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#ecfdf5',
          100: '#d1fae5',
          200: '#a7f3d0',
          300: '#6ee7b7',
          400: '#34d399',
          500: '#10b981',
          600: '#059669',
          700: '#047857',
          800: '#065f46',
          900: '#064e3b',
        },
        surface: {
          DEFAULT: '#f3f6f5',
          card: '#ffffff',
          muted: '#eef2f0',
        }
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'Inter', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        '2xl': '1.25rem',
        '3xl': '1.75rem',
        '4xl': '2.25rem',
      },
      boxShadow: {
        'soft': '0 4px 20px -2px rgba(16, 24, 40, 0.04), 0 2px 6px -2px rgba(16, 24, 40, 0.02)',
        'soft-lg': '0 12px 32px -4px rgba(16, 24, 40, 0.06), 0 4px 12px -2px rgba(16, 24, 40, 0.03)',
      }
    },
  },
  plugins: [],
}
