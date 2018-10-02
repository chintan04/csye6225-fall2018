
## Getting Started
------------------------------------------------------------------------------------------------------------------------------

We use these instructions for doing the development and testing of our CloudFormation AWS Console.

### Prerequisites required for Cloud Formation
------------------------------------------------------------------------------------------------------------------------------

You will need the following:

* VirtualBox/ VMWare Fusion
* Ubuntu Linux VM
* Pip
* [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/awscli-install-linux.html)


## In order to Run the tests
------------------------------------------------------------------------------------------------------------------------------

Make sure AWS CLI is configured with your access and secret keys. The below command will help you to provide the details for your aws setup.

```
aws configure
```
## Creating a JSON File
------------------------------------------------------------------------------------------------------------------------------

1. Create a stack with parameters and resources
2. Create VPC, subnets, internet gateway
3. Attach Internet Gateway to VPC
4. Create public route table


## Start of script for Cloud Formation
------------------------------------------------------------------------------------------------------------------------------

Make sure that the template file is in the same directory as the scripts


```
Creating the cloud formation:

> ./csye6225-aws-cf-create-stack.sh


aws cloudformation describe-stacks

{
    "Stacks": ]
}

Here you will put stack name
```

## Termination of script
------------------------------------------------------------------------------------------------------------------------------
Terminating the cloud formation:

```
Termination of the Cloud Formation

> ./csye6225-aws-cf-terminate-stack.sh

This will prompt you to ask which stack you want to delete with options, then retype the name of the stack you want to delete and hit enter


```
