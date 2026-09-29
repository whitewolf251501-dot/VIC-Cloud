/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        vic: {
          dark: '#0b0f19',
          card: '#111827',
          border: '#1f2937',
          accent: '#3b82f6',
          wolf: '#60a5fa',
        }
      }
    },
  },
  plugins: [],
}
