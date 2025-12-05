# DSA Online Student Application - DSAO
DSAO is an online portal for processing student awards applications

## Technology Stack
### Backend
* Database **Oracle 12c**
* Java **21**
* SpringBoot **3.5.0**

### Middleware
* Tomcat Server
 
### Frontend
* Thymeleaf 

## Project Structure
* **saas-dsa** - 

## Getting Started
1. Contact **SAASITSD@gov.scot** via email to request Git Bash for Windows installation on your development machine.
2. Contact Clark Bolan to create Bitbucket user and password.
3. [Generate SSH key pair](https://confluence.atlassian.com/bitbucketserver/creating-ssh-keys-776639788.html)
4. Login to Bitbucket...Manage Account...SSH keys and copy and paste your public key (C:\Users\Developer\.ssh\id_rsa.pub)
5. Globally set your git username and email, if they are absent from ```.gitconfig```

```
$ git config --global user.name "John Doe"
$ git config --global user.email johndoe@example.com
```

[More info on git initial setup](https://git-scm.com/book/en/v2/Getting-Started-First-Time-Git-Setup)

6. Navigate to your project root (eg. C:\Users\Developer\projects) and clone this repository

   `https://github.com/Student-Awards-Agency-for-Scotland/saas-dsa.git`

7. Copy maven dependencies to your local machine.

    `From: DEV_SHARE:\OLS\Dependencies\.m2 - To: C:\Users\<UserIdFolder>`

8. Import **saas-dsa** as Maven project into your IDE.

## Development Team Members (Maintenance)

Role | Person |
--- | :---:
Developer | Ranj Benning
Developer | Dimitrios Fourtounis
Developer | Siva Chimpri
Developer | Saikrishna Prathipati

## Environments
Env | Purpose | Oracle | Solaris
:--- | :--- | :---: | :---:



## Working On A Story or Defect
Below assumes your use of git command line (other git tools - IDE integrations, etc. can also be used to the same effect)
```bash
# To start work:
git checkout develop (or other relevant parent branch)
git pull origin develop
git checkout -b feature/my-feature-short-descrition
 
# Do your work and if story is taking a long time, backup to remote:
git commit -m 'my-feature Work in progress'
# Push to remote for backup
git push -u origin feature/my-feature-short-description
 
# When story is done, squash commits if more than one (only if you know how to): 
# This optional step makes sure that git history has one commit per feature which helps in rolling back features and git commands 'git blame' and 'git bisect'
git rebase -i HEAD~N
Editor specified by core.editor .gitconfig setting will open a temporary file with the last N commits, oldest being at the top. Please make sure you are not picking merge commits.
"pick" or "p" top (oldest commit) and "squash" or "s" others.
Save the file and close.
Same editor opens again allowing you to edit commit message. Create commit message for all commits - don't forget to prefix the commit message with QC/Jira ticket number.
Save and close one more time.
git push -f origin "yourbranchname".
# Reword first commit to a final meaningful message i.e. 'my-feature We now have new feature XYZ' and squash other commits
 
# To prevent merge conflicts on your PR, pull latest changes from develop:
git pull origin develop
# Resolve merge conflicts, if any
 
# If you have already pushed to remote for backup sake:
git push --force origin feature/my-feature-short-description
 
# If not and this is the first time you push to remote:
git push -u origin feature/my-feature-short-description

# Raise a PR from feature/my-feature-short-description -> develop (or other relevant parent branch)
# Address code review comments (you may need to squash commits again)
 
# After PR is merged - delete local branch
git branch -D feature/my-feature-short-description

# Rinse and repeat for next story/bugfix
```

## DSAO Release (Build and Deployment)
1. Use the following Maven command to build a new version: `mvn clean install -DskipTests` **or** `mvn clean install -Dmaven.test.skip=true` - this will build a JAR file that you will then deploy to the application servers.

2. Navigate to `target` folder and you will find the JAR file in the folder that was created on the date that you executed the clean install command

### Deploying and Testing Changes Locally
1. Run the following command with the appropriate environment 
	nohup java -jar dsao-1.0.0-SNAPSHOT.jar --spring.profiles.active=sit1 &

2. Use the following URL to register an account (info on how to create test accounts below): http://localhost:8880/dsa/start


### Register A Dummy/Test Account
1. Register using any email address using the following domain: `@saastest.co.uk` - This is linked to a gmail account, which is where you will find the activation email required to activate your test account
2. Log into Gmail account: 

    `Email address: saastesting2015@gmail.com`
    
    `Password: Student1!`
3. Click on the link in the registration email to activate the account.
4. Log into account using the links above to start testing your change.

### Restarting the Server


1. Open PuTTY and connect to the relevant host(s) 
2. Navigate to the appropriate directory: `cd /projects/app/dsa `
3. Check that processes are running using: `ps -ef | grep saasdsa`
4. Navigate to `logs` and use the command `tail -f logs/dsa-logger.log` to confirm successful startup