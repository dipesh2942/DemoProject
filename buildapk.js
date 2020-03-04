const { exec } = require('child_process');
const { existsSync, readdir, readFile, readFileSync, unlinkSync, renameSync } = require('fs');
const path = require('path');

let android = 'android/';
let androidApp = android + 'app/';
let build = androidApp + 'build/'
let bundleRelease = build + 'outputs/bundle/release';

start();

async function start() {
    if (existsSync(android + 'gradle.properties') && existsSync(bundleRelease)) {
        let { keystorePassword, aliasKey, aliasPassword } = await getProperties();
        let aabFiles = await getAabFiles();
        let keystoreFiles = await getKeyStoreFile();
        let aabFile = '', apksFile = '', zipFile = '', keystoreFileLocation = '';

        let pathSeperator = process.platform === 'win32' ? '\\' : '/';

        if (aabFiles.length > 0) {
            aabFile = path.join(__dirname, bundleRelease + pathSeperator + aabFiles[0]);
            apksFile = path.join(__dirname, bundleRelease + pathSeperator + aabFiles[0].split('.')[0] + '.apks');
            zipFile = path.join(__dirname, bundleRelease + pathSeperator + aabFiles[0].split('.')[0] + '.zip');
        }

        if (keystoreFiles.length > 0) {
            keystoreFileLocation = path.join(__dirname, androidApp + pathSeperator + keystoreFiles[0])
        }

        let bundleFile = ' --bundle=' + aabFile;
        let outputFile = ' --output=' + apksFile;
        let keystoreFile = ' --ks=' + keystoreFileLocation;
        let ksPass = ' --ks-pass=pass:' + keystorePassword;
        let ksKeyAlias = ' --ks-key-alias=' + aliasKey;
        let keyPass = ' --key-pass=pass:' + aliasPassword;
        let bundleToolCommand = 'bundletool' + (process.platform === 'win32' ? '.jar' : '') + ' build-apks' + bundleFile + outputFile + keystoreFile + ksPass + ksKeyAlias + keyPass + ' --mode=universal';

        await execute(bundleToolCommand)

        if (existsSync(apksFile)) {

            console.log('APKS File Created Successfully\n');

            renameSync(apksFile, zipFile);

            if (existsSync(zipFile)) {
                if (process.platform === 'win32') {
                    await execute('PowerShell Expand-Archive -Path ' + zipFile + ' -DestinationPath ' + bundleRelease + ' -Force');
                } else {
                    await execute('cd ' + bundleRelease + ' && unzip ' + zipFile);
                }
                unlinkSync(zipFile);
            }

            if (existsSync(bundleRelease + '/toc.pb')) {
                unlinkSync(bundleRelease + '/toc.pb');
            }

            if (existsSync(bundleRelease + '/' + getFileName())) {
                unlinkSync(bundleRelease + '/' + getFileName());
            }

            if (existsSync(bundleRelease + '/universal.apk')) {
                renameSync(bundleRelease + '/universal.apk', bundleRelease + '/' + getFileName())
                console.log('Generated Apk File Successfully')
            }
        }
    }
}

function execute(command) {
    return new Promise(resolve => {
        exec(command, (_error, stdout) => {
            resolve(stdout);
        })
    })
}

function getFileName() {
    var today = new Date();
    var dd = today.getDate();
    var mm = today.getMonth() + 1; //January is 0!

    var yyyy = today.getFullYear();
    if (dd < 10) {
        dd = '0' + dd;
    }
    if (mm < 10) {
        mm = '0' + mm;
    }
    var today = dd + mm + yyyy;

    let rawdata = readFileSync('app.json');
    let appName = JSON.parse(rawdata).name;

    return appName + '_' + today + '.apk';
}

function getProperties() {

    return new Promise(resolve => {
        // Read entire file
        readFile(android + 'gradle.properties', 'utf8', function (err, data) {

            // check for error
            if (err) {
                resolve({});
            }

            let properties = {};

            let arrayData = data.split('\n');
            for (let index = 0; index < arrayData.length; index++) {
                const element = arrayData[index];

                if (element.includes('MYAPP_RELEASE_STORE_FILE')) {
                    properties.keystoreFile = element.split('=')[1]
                }
                if (element.includes('MYAPP_RELEASE_KEY_ALIAS')) {
                    properties.aliasKey = element.split('=')[1]
                }
                if (element.includes('MYAPP_RELEASE_STORE_PASSWORD')) {
                    properties.keystorePassword = element.split('=')[1]
                }
                if (element.includes('MYAPP_RELEASE_KEY_PASSWORD')) {
                    properties.aliasPassword = element.split('=')[1]
                }
            }

            resolve(properties)
        });
    })

}

function getAabFiles() {

    return new Promise(resolve => {
        //joining path of directory
        const directoryPath = path.join(__dirname, bundleRelease);

        //passsing directoryPath and callback function
        readdir(directoryPath, function (err, files) {
            //handling error
            if (err) {
                resolve([]);
            }

            let aabFiles = [];
            //listing all files using forEach
            files.forEach(function (file) {
                // Do whatever you want to do with the file
                if (file.includes('.aab')) {
                    aabFiles.push(file);
                }
            });
            resolve(aabFiles);
        });
    })
}

function getKeyStoreFile() {

    return new Promise(resolve => {
        //joining path of directory
        const directoryPath = path.join(__dirname, androidApp);

        //passsing directoryPath and callback function
        readdir(directoryPath, function (err, files) {
            //handling error
            if (err) {
                return console.log('Unable to scan directory: ' + err);
            }

            let keystoreFiles = [];
            //listing all files using forEach
            files.forEach(function (file) {
                // Do whatever you want to do with the file
                if (file.includes('.keystore') && !file.includes('debug.keystore')) {
                    keystoreFiles.push(file);
                }
            });

            resolve(keystoreFiles);
        });
    })

}

