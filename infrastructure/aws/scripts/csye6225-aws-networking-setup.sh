#!/bin/bash
# create-aws-vpc
availabilityZone1="us-east-1d"
availabilityZone2="us-east-1e"
availabilityZone3="us-east-1f"

echo "Creating VPC:"
#create vpc with cidr block /16
echo "Enter the CIDR block for VPC"
read vpcCidrBlock
aws_response1=$(aws ec2 create-vpc --cidr-block "$vpcCidrBlock")
vpcId=$(echo -e "$aws_response1" | grep VpcId | awk '{print $2}' | tr -d '"' | tr -d ',')
if [ $? -ne "0" ]
then
  echo "Error with CIDR block. Please check"
  exit 1
fi



#name the vpc
echo "Enter the name for VPC"
read vpcName
aws ec2 create-tags --resources "$vpcId" --tags Key=Name,Value="$vpcName"
if [ $? -ne "0" ]
then
  echo "Name already exists. Please use some other name."
  exit 1
fi


#create internet gateway
gateway_response=$(aws ec2 create-internet-gateway --output json)
gatewayId=$(echo -e "$gateway_response" | grep igw- | awk '{print $2}' | tr -d '"' | tr -d ',')

#name the internet gateway
echo "Enter the name for the internet-gateway"
read gatewayName
aws ec2 create-tags --resources "$gatewayId" --tags Key=Name,Value="$gatewayName"
#attach gateway to vpc
attach_response=$(aws ec2 attach-internet-gateway --internet-gateway-id "$gatewayId" --vpc-id "$vpcId")
if [ $? -ne "0" ]
then
  echo "Error in internet gateway creation. Please check"
  exit 1
else
  echo "Internet gateway created successfully!"
fi



#subnet1
echo "Enter the CIDR for first public subnet"
read subNet1
publicsubnet1=$(aws ec2 create-subnet --cidr-block "$subNet1" --availability-zone "$availabilityZone1" --vpc-id "$vpcId" --output json )
pubsubnetId1=$(echo -e "$publicsubnet1" | grep subnet- | awk '{print $2}' | tr -d '"' | tr -d ',')
aws ec2 create-tags --resources "$pubsubnetId1" --tags Key=Name,Value="pub_$availabilityZone1"
if [ $? -ne "0" ]
then
  echo "Error! First public subnet creation failed!. Please check"
  exit 1
fi


#enable public ip on subnet
modify_response=$(aws ec2 modify-subnet-attribute --subnet-id "$pubsubnetId1" --map-public-ip-on-launch)

#subnet2
echo "Enter the CIDR for first private subnet"
read subNet2
privatesubnet1=$(aws ec2 create-subnet --cidr-block "$subNet2" --availability-zone "$availabilityZone1" --vpc-id "$vpcId" --output json)
prisubnetId1=$(echo -e "$privatesubnet1" | /usr/bin/json_reformat | tr -d '",:' | grep subnet- |  awk '{print $2;}')
aws ec2 create-tags --resources "$prisubnetId1" --tags Key=Name,Value="pri_$availabilityZone1"
if [ $? -ne "0" ]
then
  echo "Error! First private Subnet creation failed!. Please check"
  exit 1
fi

#subnet3
echo "Enter the CIDR for second public subnet"
read subNet3
publicsubnet2=$(aws ec2 create-subnet --cidr-block "$subNet3" --availability-zone "$availabilityZone2" --vpc-id "$vpcId" --output json)
pubsubnetId2=$(echo -e "$publicsubnet2" | /usr/bin/json_reformat | tr -d '",:' | grep subnet- |  awk '{print $2;}')
aws ec2 create-tags --resources "$pubsubnetId2" --tags Key=Name,Value="pub_$availabilityZone2"
modify_response=$(aws ec2 modify-subnet-attribute --subnet-id "$pubsubnetId2" --map-public-ip-on-launch)
if [ $? -ne "0" ]
then
  echo "Error! second public Subnet creation failed!. Please check"
  exit 1
fi

#subnet4
echo "Enter the CIDR for second private subnet"
read subNet4
privatesubnet2=$(aws ec2 create-subnet --cidr-block "$subNet4" --availability-zone "$availabilityZone2" --vpc-id "$vpcId" --output json)
prisubnetId2=$(echo -e "$privatesubnet2" | /usr/bin/json_reformat | tr -d '",:' | grep subnet- |  awk '{print $2;}')
aws ec2 create-tags --resources "$prisubnetId2" --tags Key=Name,Value="pri_$availabilityZone2"
if [ $? -ne "0" ]
then
  echo "Error! second private Subnet creation failed!. Please check"
  exit 1
fi

#subnet5
echo "Enter the CIDR for third public subnet"
read subNet5
publicsubnet3=$(aws ec2 create-subnet --cidr-block "$subNet5" --availability-zone "$availabilityZone3" --vpc-id "$vpcId" --output json)
pubsubnetId3=$(echo -e "$publicsubnet3" | /usr/bin/json_reformat | tr -d '",:' | grep subnet- |  awk '{print $2;}')
aws ec2 create-tags --resources "$pubsubnetId3" --tags Key=Name,Value="pub_$availabilityZone3"
modify_response=$(aws ec2 modify-subnet-attribute --subnet-id "$pubsubnetId3" --map-public-ip-on-launch)
if [ $? -ne "0" ]
then
  echo "Error! third public Subnet creation failed!. Please check"
  exit 1
fi

#subnet6
echo "Enter the CIDR for third private subnet"
read subNet6
privatesubnet3=$(aws ec2 create-subnet --cidr-block "$subNet6" --availability-zone "$availabilityZone3" --vpc-id "$vpcId" --output json)
prisubnetId3=$(echo -e "$privatesubnet3" | /usr/bin/json_reformat | tr -d '",:' | grep subnet- |  awk '{print $2;}')
aws ec2 create-tags --resources "$prisubnetId3" --tags Key=Name,Value="pri_$availabilityZone3"
if [ $? -ne "0" ]
then
  echo "Error! third private Subnet creation failed!. Please check"
  exit 1
else
  echo "Subnets created successfully!"
fi

#create route table for vpc
route_table_response=$(aws ec2 create-route-table --vpc-id "$vpcId" --output json)
routeTableId=$(echo -e "$route_table_response" | /usr/bin/json_reformat | tr -d '",:' | grep rtb- |  awk '{print $2;}')
if [ $? -ne "0" ]
then
  echo "Route table creation failed!. Please check"
  exit 1
else
  echo "Route table created successfully!"
fi

#name the route table
echo "Enter the name for the route table"
read routeTableName
aws ec2 create-tags --resources "$routeTableId" --tags Key=Name,Value="$routeTableName"
if [ $? -ne "0" ]
then
  echo "Name already exists. Please check"
  exit 1
fi


#add route for the internet gateway
route_response=$(aws ec2 create-route --route-table-id "$routeTableId" --destination-cidr-block 0.0.0.0/0 --gateway-id "$gatewayId")


#add route to subnet (all are public subnets)
associate_response=$(aws ec2 associate-route-table --subnet-id "$pubsubnetId1" --route-table-id "$routeTableId")
associate_response=$(aws ec2 associate-route-table --subnet-id "$pubsubnetId2" --route-table-id "$routeTableId")
associate_response=$(aws ec2 associate-route-table --subnet-id "$pubsubnetId3" --route-table-id "$routeTableId") 
if [ $? -ne "0" ]
then
  echo "Cannot add route to the subnet! Please check"
  exit 1
else
  echo "Routes added created successfully!"
fi


echo " "
echo "VPC created..!!!"

