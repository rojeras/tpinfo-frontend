config.resolve.modules.push("../../processedResources/js/main");

if (config.devServer) {
    config.devServer.hot = true;
    config.devtool = 'eval-cheap-source-map';
    config.devServer.host = '0.0.0.0';
} else {
    config.devtool = undefined;
}
