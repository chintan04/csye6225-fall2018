#!/bin/bash
#getting vpc id
echo "Deletion wizard!!"
echo "Enter the name of vpc to be deleted"
read myVPC
vpcdesc=$(aws ec2 describe-vpcs --filters "Name=tag:Name, Values=$myVPC")
vpcId=$(echo -e "$vpcdesc" | grep VpcId | awk '{print $2}' | tr -d '"' | tr -d ',')
if [ $? -ne "0" ]
then
  echo "Error! Please check the name"
  exit 1
fi

#deleting subnet
subdesc=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$vpcId")
echo -e "$subdesc" | grep subnet- | awk '{print $2}' | tr -d '"' | tr -d ',' > subids.txt
len=`wc -l <subids.txt`
i=1
while [ $i -le $len ];
do
	iD=$(awk NR=="$i" subids.txt)
	#echo "$iD"
	aws ec2 delete-subnet --subnet-id "$iD"
  	i=$(($i + 1))
done


if [ $? -ne "0" ]
then
  echo "Error in deleting the subnets!"
  exit 1
else
  echo "Subnets deleted successfully!"
fi
#deleting Routetable
#roudesc=$(aws ec2 describe-route-tables --filters "Name=tag:Name, Values=myRouteTable")
#routeid=$(echo -e "$roudesc" | grep rtb- | awk '{print $2}' | tr -d '"' | tr -d ','| awk NR==1)
routeid=$(aws ec2 describe-route-tables --filter Name=vpc-id,Values=$vpcId --query 'RouteTables[?Associations[0].Main != `true`].RouteTableId' | tr -d '"' | tr -d ',[]')
aws ec2 delete-route-table --route-table-id $routeid
if [ $? -ne "0" ]
then
  echo "Error in deleting the Route table"
  exit 1
else
  echo "Route table deleted successfully!"
fi
#detaching internet gateway
#igw=$(aws ec2 describe-internet-gateways --filters "Name=tag:Name,Values=myIG")
#igwid=$(echo -e "$igw" | grep igw- | awk '{print $2}' | tr -d '"' | tr -d ',')
igwid=$(aws ec2 describe-internet-gateways --filter Name=attachment.vpc-id,Values=$vpcId --query 'InternetGateways[].InternetGatewayId' | tr -d '"' | tr -d ',[]')
aws ec2 detach-internet-gateway --internet-gateway-id "$igwid" --vpc-id "$vpcId"
if [ $? -ne "0" ]
then
  echo "Error in detaching the internet gateway"
  exit 1
fi
aws ec2 delete-internet-gateway --internet-gateway-id $igwid
if [ $? -ne "0" ]
then
  echo "Error in deleting the internet gateway"
  exit 1
else
  echo "Internet gateway deleted successfully!"
fi

#deleting vpc
vpcdesc=$(aws ec2 describe-vpcs --filters "Name=tag:Name, Values=$myVPC")
vpcId=$(echo -e "$vpcdesc" | grep VpcId | awk '{print $2}' | tr -d '"' | tr -d ',')
aws ec2 delete-vpc --vpc-id "$vpcId"
if [ $? -ne "0" ]
then
  echo "Error in deleting VPC!"
  exit 1
else
	echo "VPC deleted successfully..!!"
fi


