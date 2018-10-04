#!/bin/bash

Stack_list=$(aws cloudformation list-stacks --stack-status-filter CREATE_COMPLETE --query 'StackSummaries[].StackName' --output text )
echo Stack list: \n $Stack_list
delete(){
    read stack

    id=$(aws cloudformation describe-stacks --stack-name $stack --query "Stacks[*].StackId" --output text 2>&1)

    aws cloudformation delete-stack --stack-name $stack

    aws cloudformation wait stack-delete-complete --stack-name $stack
    aws cloudformation describe-stacks --stack-name $id --query "Stacks[*].StackStatus" --output text
}
echo Alert! Please select the EC2 instance stack first.
delete
echo Now select other stack
delete
exit 0
