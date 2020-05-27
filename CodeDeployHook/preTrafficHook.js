// Stolen from https://aws.amazon.com/blogs/compute/implementing-safe-aws-lambda-deployments-with-aws-codedeploy/
'use strict';

const AWS = require('aws-sdk');
const codeDeploy = new AWS.CodeDeploy({ apiVersion: '2014-10-06' });
var lambda = new AWS.Lambda({httpOptions: {timeout: 30000}});

exports.handler = (event, context, callback) => {
	var debug = process.env.DEBUG === 'true';
	var isCodeDeployRun = false, forceError = false;
	if (event.DeploymentId){
		isCodeDeployRun = true;
	} else if (event.forceError == 'true'){
		forceError = true;
	}
	if (debug){console.log("Entering PreTraffic Hook!");}

	var functionToTest = process.env.FUNCTION_TO_TEST;
	if (debug){console.log("Testing new function version: " + functionToTest + ", isCodeDeployRun: " + isCodeDeployRun + ", forceError: " + forceError);}

	// Perform validation of the newly deployed Lambda version
	var lambdaResult = "Failed";
	var lambdaParams = {
		FunctionName: functionToTest,
		InvocationType: "RequestResponse",
		Payload: '{"action":"next","currentPlayer":"r","board":{"numCols":7,"numRows":6,"rows":[["r","r","y",".",".",".","."],["y","y","y",".",".",".","."],["y","y","y",".",".",".","."],["r",".","r",".",".",".","."],[".",".",".",".",".",".","."],[".",".",".",".",".",".","."]]},"column":6}'
	};
	lambda.invoke(lambdaParams, function (err, data){
		if (err){ // an error occurred
			console.error(err, err.stack);
			lambdaResult = "Failed";
		} else { // successful response
			if (debug){console.log("Result... Payload: " + data.Payload);}
			if (data.Payload == '{"gameState":"0","playerBoard":{"numCols":7,"numRows":6,"rows":[["r","r","y",".",".",".","r"],["y","y","y",".",".",".","."],["y","y","y",".",".",".","."],["r",".","r",".",".",".","."],[".",".",".",".",".",".","."],[".",".",".",".",".",".","."]]},"playerRow":0,"aiBoard":{"numCols":7,"numRows":6,"rows":[["r","r","y","y",".",".","r"],["y","y","y",".",".",".","."],["y","y","y",".",".",".","."],["r",".","r",".",".",".","."],[".",".",".",".",".",".","."],[".",".",".",".",".",".","."]]},"aiCol":3,"aiRow":0}'){
				lambdaResult = "Succeeded";
				if (debug){console.log("Validation testing succeeded!");}
			} else {
				lambdaResult = "Failed";
				console.error("Validation testing failed!");
			}

			if (isCodeDeployRun){
				// Pass AWS CodeDeploy the prepared validation test results.
				var params = {
					deploymentId: event.DeploymentId,
					lifecycleEventHookExecutionId: event.LifecycleEventHookExecutionId,
					status: lambdaResult
				};
				codeDeploy.putLifecycleEventHookExecutionStatus(params, function (err, data){
					if (err){
						// Validation failed.
						console.error('CodeDeploy Status update failed');
						console.error(err, err.stack);
						callback("CodeDeploy Status update failed");
					} else {
						// Validation succeeded.
						if (debug){console.log('CodeDeploy status updated successfully');}
						callback(null, 'CodeDeploy status updated successfully');
					}
				});
			} else if (lambdaResult == "Failed" || forceError){
				// Hand-cranked synthetic canary run and it failed
				var sns = new AWS.SNS();
				var params = {
					Message: 'Hand-cranked canary test for Connect4Trainer failed. See https://ap-southeast-2.console.aws.amazon.com/cloudwatch/home?region=ap-southeast-2#dashboards:name=Connect4',
					Subject: 'AWS:Sydney - Connect4Trainer testing call failed',
					TopicArn: process.env.SNS_TOPIC_ARN
				};
				sns.publish(params, function(err, data) {
					if (err){
						console.error('Hand-cranked canary test failed and could not send SNS topic');
						console.error(err, err.stack);
					} else {
						if (debug){console.log('Hand-cranked canary test failed and SNS topic sent!');}
					}
				});
			}
		}
	});
}