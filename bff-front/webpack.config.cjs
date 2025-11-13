const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const { ModuleFederationPlugin } = require('webpack').container;

module.exports = {
  entry: path.resolve(__dirname, 'src', 'index.tsx'),
  mode: process.env.NODE_ENV || 'development',
  devServer: { port: 3000, historyApiFallback: true, static: path.resolve(__dirname, 'dist') },
  output: { publicPath: 'auto', clean: true },
  resolve: { extensions: ['.tsx', '.ts', '.js'] },
  module: { rules: [{ test: /\.tsx?$/, loader: 'ts-loader', exclude: /node_modules/ }] },
  plugins: [
    new ModuleFederationPlugin({
      name: 'bff_front',
      filename: 'remoteEntry.js',
      exposes: { './App': './src/App' },
      remotes: {
        payments: 'payments@https://bff-payments.example.com/remoteEntry.js',
        header: 'header@https://bff-header.example.com/remoteEntry.js',
        footer: 'footer@https://bff-footer.example.com/remoteEntry.js'
      },
      shared: {
        react: { singleton: true, requiredVersion: '^18.2.0' },
        'react-dom': { singleton: true, requiredVersion: '^18.2.0' },
        axios: { singleton: true }
      }
    }),
    new HtmlWebpackPlugin({ template: path.resolve(__dirname, 'public', 'index.html') })
  ]
};
