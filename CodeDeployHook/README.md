# CodeDeploy Hook

This function redirects is run by CodeDeploy during CloudFormation stack updates. It validates that the new version of the Lambda function returns the correct response before updating the "live" alias to the new version.

Limitations:

* Only tests the Lambda function, doesn't test API Gateway