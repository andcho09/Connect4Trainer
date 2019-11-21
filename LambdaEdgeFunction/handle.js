'use strict';

exports.handler = (event, context, callback) => {
	// Lambda@Edge doesn't support environment variables :(
	const isDebug = false;
	const alternateEndpoint = '<abc123>.execute-api.<alternate-region>.amazonaws.com'; 
	
	const request = event.Records[0].cf.request;
	var debugOutput = 'uri=' + request.uri;
	if (request.uri.startsWith('/game/')){
		// Request will go to API Gateway (don't care about S3 since that's CloudFront cached)
		if (request.headers['cloudfront-viewer-country']) {
			const countryCode = request.headers['cloudfront-viewer-country'][0].value;
			debugOutput += ', country=' + countryCode;
			if (countryCode === 'AU' || countryCode === 'NZ') {
				// Switch to Sydney region (default is North Virginia)
				request.origin.custom.domainName = alternateEndpoint;
				request.headers['host'] = [{key: 'host', value: alternateEndpoint}];
				debugOutput += ', redirect!';
			}
		}
	}
	if (isDebug){
		console.log(debugOutput);
	}
	callback(null, request);
};