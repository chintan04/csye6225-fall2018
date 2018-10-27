#!/bin/bash
echo Provide name of the stack to create VPC
read stack

aws cloudformation create-stack --stack-name $stack --template-body file://csye6225-cf-networking.json --parameters file://vpcStackParameter.json

aws cloudformation wait stack-create-complete --stack-name $stack

aws cloudformation describe-stacks --stack-name $stack --query "Stacks[*].StackStatus" --output text

exit 0

