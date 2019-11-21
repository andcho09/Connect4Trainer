# Lambda@Edge Function To Redirect Users To Closest Region

This function redirects ``/game/`` requests to the closest region based on the ``CloudFront-Viewer-Country`` HTTP header. Default is to send to us-east-1.

## Notes

* This function is deployed manually since it's cross-region. It should only need to be modified if the Sydney API Gateway URL changes
* Lambda@Edge functions do not support environment variables which means we have to hard-code :(
* Since this needs the ``CloudFront-Viewer-Country`` HTTP header, it must be deployed at the ``Origin Request`` stage
