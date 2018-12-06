# csye6225-fall2018
The application implements APIs using Java programming language and Spring boot framework with PostgreSQL as the persistent backend data store.

# Getting Started
Follow the steps below to run the project on your local machine for development and testing purposes. Refer the deployment(AWS) section for deploying the application to AWS

# Prerequisites for building and deploying the application locally :
- Postgresql db should be up & running with db name: csye6225
- Postman should be installed and up to verify the application

# Installation:
- Git Clone the required repository
- Import the code source from the cloned repo in Intellij
- Resolve all the maven dependecies.

# Deployment (Local):
- Runner class contains the main method.
- Right click on the file and click run to start the application on the local server.

# Deployment (AWS):
## Build with TravisCI
- [Travis CI](https://travis-ci.com/) - Travis CI is a hosted, distributed continuous integration service used to build and test software projects hosted at GitHub.
## Steps for Travis CI build
- Login to Travis CI (using github login)
- Activate your repository
- Flick the repository switch on
- Add .travis.yml file to your repository
- Make a change to the existing code and push the changes to github using "git push". This will automatically trigger a build.

# Instructions to run tests :
- From the maven window select lifecycle
- Select test under lifecycle & run maven

# Instructions to run Jmeter script :
- Launch the jmeter application
- Import the .jmx file from and make all necessary changes under thread group i.e server name, csv data filename, number of threads etc.
- Hit start and make sure you are able to see successful response under the section "View Results Tree"

# Team Members :
1. Sayali Gaikawad  -   gaikawad.s@husky.neu.edu
2. Akul Nigam       -   nigam.a@husky.neu.edu
3. Pankaj Sahani    -   sahani.p@husky.neu.edu
4. Chintan Shah     -   shah.c@husky.neu.edu

# License :
This project is licensed under Northeastern University

# Acknowledgments:
- Prof. Tejas Parikh for providing sample templates and constant support
- TA's Bhumika, Varsha, Chintan and Nishant for guidance
