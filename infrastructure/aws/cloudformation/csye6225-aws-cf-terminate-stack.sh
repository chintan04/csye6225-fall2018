#!/bin/bash

Stack_list=$(aws cloudformation list-stacks --stack-status-filter CREATE_COMPLETE --query 'StackSummaries[].StackName' --output text )
echo $Stack_list
echo stack to delete
read stack

id=$(aws cloudformation describe-stacks --stack-name $stack --query "Stacks[*].StackId" --output text 2>&1)

aws cloudformation delete-stack --stack-name $stack

aws cloudformation wait stack-delete-complete --stack-name $stack
aws cloudformation describe-stacks --stack-name $id --query "Stacks[*].StackStatus" --output text

exit 0
