#!/bin/bash
echo provide name of the stack
read stack


#name=$(aws cloudformation describe-stacks --stack-name $var1 --query "Stacks[*].StackName" --output text 2>&1)
#echo $?

# name=$(aws cloudformation wait stack-exists --stack-name $var1 2>&1)
# validation code
# if [[ -z $name ]];then
# 	echo "the stack exists. please enter a different name"
# 	exit 0
# fi

aws cloudformation create-stack --stack-name $stack --template-body file://csye6225-cf-networking.json --parameters file://parameters1.json

aws cloudformation wait stack-create-complete --stack-name $stack

aws cloudformation describe-stacks --stack-name $stack --query "Stacks[*].StackStatus" --output text

exit 0
