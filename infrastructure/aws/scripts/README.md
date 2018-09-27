# Scripts

This folder deals with the setting up AWS Infrastructure using CLI commands.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for building up the AWS infrastructure. See deployment for notes on how to deploy the script on your machine.

### Prerequisites

You would require:

* VirtualBox or VMWare Workstation
* Ubuntu Linux VM
* Pip
* [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/awscli-install-linux.html)

```
sudo apt-get install python-pip
```


## Running the tests

Make sure AWS CLI is configured with your access and secret keys. The below command will help you to provide the details for your aws setup.

```
aws configure
```


### Deployment

Make sure that the you are in the same directory as the script file or you can provide path of the script file running it.


```
Running the buildup script

> ./csye6225-aws-networking-setup.sh

```

```
For deleting the built infrastructure

./csye6225-aws-networking-teardown.sh
```
