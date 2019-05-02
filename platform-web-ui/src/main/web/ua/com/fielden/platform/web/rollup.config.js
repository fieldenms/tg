import rootImport from 'rollup-plugin-root-import';

export default {
  input: 'desktop-startup-resources-origin.js',
  output: {
    file: 'desktop-startup-resources-vulcanized.js',
    format: 'esm'
  },
  plugins: [
    rootImport({
      // Will first look in `client/src/*` and then `common/src/*`.
      root: `${__dirname}`,
      useEntry: 'prepend'
    })
  ]
};
