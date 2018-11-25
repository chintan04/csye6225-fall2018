#!/bin/bash
echo Provide the name for EC2 Stack
read stack1
aws cloudformation create-stack --stack-name $stack1 --template-body file://csye6225-cf-application.json --parameters file://ec2ApplicationStackParameter.json --capabilities CAPABILITY_NAMED_IAM

aws cloudformation wait stack-create-complete --stack-name $stack1

aws cloudformation describe-stacks --stack-name $stack1 --query "Stacks[*].StackStatus" --output text

exit 0
