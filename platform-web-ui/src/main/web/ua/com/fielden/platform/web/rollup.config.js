import rootImport from 'rollup-plugin-root-import';

export default {
  input: '@profile-startup-resources-origin.js',
  output: {
    file: '@profile-startup-resources-vulcanized.js',
    format: 'esm'
  },
  plugins: [
    rootImport({
      root: `${__dirname}/vulcan`,
      useEntry: 'prepend'
    })
  ]
};
