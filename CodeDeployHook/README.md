# CodeDeploy Hook

This function is run by CodeDeploy during ``Connect4TrainerFunction`` CloudFormation stack updates. It validates that the new version of the Lambda function returns the correct response before updating the "live" alias to the new version.

See parent [README.md](../README.md) for deploy instructions.

Limitations:

* Only tests the Lambda function, doesn't test API Gateway