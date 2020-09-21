config.resolve.modules.push("../../processedResources/js/main");

if (config.devServer) {
    config.devServer.stats = {
        warnings: false
    };
    config.devServer.clientLogLevel = 'error';
    config.devtool = 'eval-cheap-source-map';
    config.devServer.host = '0.0.0.0';
}

/* Changed migrating to KVision 3.13.1
config.resolve.modules.push("../../processedResources/Js/main");
if (!config.devServer && config.output) {
    config.devtool = false
    config.output.filename = "main.bundle.js"
}
if (config.devServer) {
    config.devServer.watchOptions = {
        aggregateTimeout: 1000,
        poll: 500
    };
    config.devServer.stats = {
        warnings: false
    };
    config.devServer.clientLogLevel = 'error';
    config.devServer.host = '0.0.0.0';
}
 */
