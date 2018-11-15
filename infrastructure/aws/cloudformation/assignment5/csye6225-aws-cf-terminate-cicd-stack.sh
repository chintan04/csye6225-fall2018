#!/bin/bash

#var1="$1"
#name=$(aws cloudformation describe-stacks --stack-name $var1 --query "Stacks[*].StackName" --output text 2>&1)

#validation code
# name=$(aws cloudformation wait stack-exists --stack-name $var1 2>&1)
# if [[ ! -z $name ]];then
# 	echo "the stack does not exist. please enter a different name"
# 	exit 0
# fi


Stack_list=$(aws cloudformation list-stacks --stack-status-filter CREATE_COMPLETE --query 'StackSummaries[].StackName' --output text )
echo $Stack_list
echo stack to delete
read stack

id=$(aws cloudformation describe-stacks --stack-name $stack --query "Stacks[*].StackId" --output text 2>&1)


bucket_name=$(aws route53 list-hosted-zones --query HostedZones[].{Name:Name} --output text|sed 's/.$//')

bucket=code-deploy.$bucket_name
echo $bucket

#delete code deploy bucket
#bucket_id=$(aws cloudformation describe-stack-resources --stack-name $var1 --logical-resource-id myS3Bucket --query "StackResources[0].PhysicalResourceId" --output text)
aws s3 rb s3://$bucket --force

aws cloudformation delete-stack --stack-name $stack

aws cloudformation wait stack-delete-complete --stack-name $stack

echo delete successful
#aws cloudformation describe-stacks --stack-name cicd --query "Stacks[*].StackStatus" --output text

exit 0