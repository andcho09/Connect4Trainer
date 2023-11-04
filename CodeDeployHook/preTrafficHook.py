import boto3
import logging
import os

code_deploy_client = boto3.client('codedeploy')
lambda_client = boto3.client('lambda')
sns_client = boto3.client('sns')

def handler(event, context):
	# Logger setup
	logger = logging.getLogger()
	if os.getenv('DEBUG') == 'true':
		logger.setLevel(logging.DEBUG)

	logger.debug("Entering PreTraffic Hook!")

	# Init
	code_deployment_deployment_id = None
	force_error = False
	new_function_successful = False
	if 'DeploymentId' in event:
		code_deployment_deployment_id = event['DeploymentId']
	elif 'forceError' in event and event['forceError'] == 'true':
		force_error = False
	function_to_test = os.getenv('FUNCTION_TO_TEST')

	logger.debug(f"Testing new function version: {function_to_test}, isCodeDeployRun: {code_deployment_deployment_id != None}, forceError: {force_error}")

	response = lambda_client.invoke(
		FunctionName = function_to_test,
		Payload = '{"action":"next","currentPlayer":"r","board":{"numCols":7,"numRows":6,"rows":[["r","r","y",".",".",".","."],["y","y","y",".",".",".","."],["y","y","y",".",".",".","."],["r",".","r",".",".",".","."],[".",".",".",".",".",".","."],[".",".",".",".",".",".","."]]},"column":6}'
	)

	# Check if the response from the new function is correct
	if response['StatusCode'] != 200:
		logger.error(f"New function failed. Status code: {response['StatusCode']}. Error: {response['FunctionError']}")
	else:
		payload = response['Payload'].read().decode("utf-8").strip()
		if payload == '{"gameState":"0","playerBoard":{"numCols":7,"numRows":6,"rows":[["r","r","y",".",".",".","r"],["y","y","y",".",".",".","."],["y","y","y",".",".",".","."],["r",".","r",".",".",".","."],[".",".",".",".",".",".","."],[".",".",".",".",".",".","."]]},"playerRow":0,"aiBoard":{"numCols":7,"numRows":6,"rows":[["r","r","y","y",".",".","r"],["y","y","y",".",".",".","."],["y","y","y",".",".",".","."],["r",".","r",".",".",".","."],[".",".",".",".",".",".","."],[".",".",".",".",".",".","."]]},"aiCol":3,"aiRow":0}':
			logger.info(f"New function passed successfully")
			new_function_successful = True
		else:
			logger.error(f"New function completed but response payload is wrong: {payload}")


	# Return CodeDeploy response
	if code_deployment_deployment_id is not None:
		response = code_deploy_client.put_lifecycle_event_hook_execution_status(
			deploymentId = code_deployment_deployment_id,
			lifecycleEventHookExecutionId = event['LifecycleEventHookExecutionId'],
			status = 'Succeeded' if new_function_successful else 'Failed'
		)
		if 'lifecycleEventHookExecutionId' in response:
			logger.info('Completed CodeDeploy lifecycle')
		else:
			logger.error(f"Failed to complete CodeDeploy lifecycle for deploymentId: {code_deployment_deployment_id} and lifecycleEventHookExecutionId: {event['LifecycleEventHookExecutionId']}")
	elif not new_function_successful or force_error:
		# Not doing CodeDeploy, so fire SNS if there's a failure
		logger.debug(f"Publishing failure notification to topic: {os.getenv('SNS_TOPIC_ARN')}")
		response = sns_client.publish(
			TopicArn = os.getenv('SNS_TOPIC_ARN'), # This might fail if we're not publishing to a topic in the same region
			Message = 'Hand-cranked canary test for Connect4Trainer failed. See https://ap-southeast-2.console.aws.amazon.com/cloudwatch/home?region=ap-southeast-2#dashboards:name=Connect4',
			Subject = 'Connect4Trainer testing call failed'
		)
		if 'MessageId ' not in response:
			logger.error(f"Hand-cranked canary test failed and could not send SNS topic. Response: {response}")
		else:
			logger.debug(f"Hand-cranked canary test failed and SNS topic sent. MessageId: {response['MessageId']}")
