# tpinfo-frontend

tpinfo-frontend consists of two web application showing information about integration and number of calls passing through [RIV-TA](https://rivta.se/documents.html#003) based service platforms. 
The applications are:  

**hippo** - https://integrationer.tjansteplattform.se/  
**statistik** - https://statistik.tjansteplattform.se/

(The backend reside in its own repository, see https://github.com/rojeras/tpinfo-backend)  

The Applications in tpinfo-frontend are defined as separate views in a shared code base.  

## Building blocks
tpinfo-frontend is implemented in [Kotlin](https://kotlinlang.org/). Kotlin is a modern, functional, strongly typed language. Kotlin is used on many platforms, not the least Android where Google is promoting it. The language can also be used for web development, Kotlin/JS. It was selected for tpinfo as a better alternative than Javascript.  
[KVision](https://kvision.io/) was further selected as the base framework. It contains a large number of ready to use GUI components and interfaces to known Javascript libraries. In tpinfo the following are used:  
* [Bootstrap](https://getbootstrap.com/) - GUI components
* [Redux](https://redux.js.org/) (Javascript Redux, not ReduxKotlin) - as a base for the reactive GUIs
* [Navigo](https://github.com/krasimir/navigo) - Browser navigation and history
* [Tabulator](http://tabulator.info/) - interactive tables in Statistik
* [Chart.js](https://www.chartjs.org/) - the charts in Statistik
* [Docker](https://www.docker.com/) - as execution environment

## Development 
### Setup
tpinfo is developed with [IntelliJ Ultimate](https://www.jetbrains.com/idea/). It is an amazing development environment. In addition you need to install up to date versions of [git](https://git-scm.com/) and [gradle](https://gradle.org/) (I think).  

Then, to get started, just clone this repo and open it as a project in IntelliJ. Use the gradle tasks to control the development process. 

#### Debug
When starting from the template project you need to remove line
```sourceMaps = false``` from build.gradle.kts and remove line ```config.devtool = 'eval-cheap-source-map'``` from webpack.config.d/webpack.js.
After this you should see all kotlin sources in the Sources panel of Chrome dev tools.
And also you will see correct file name and line number in the console panel when you do console.log() in your code.  
This step has been performed in tpinfo-frontend.

### Participate in tpinfo development 
This is a preliminary process how to participate and make changes in this tpinfo repo.

1. Identify a change you want to make
1. Log on to github
1. Go to the `rojeras/tpinfo-frontend` repo
1. Open an issue and describe the change. Wait for feedback from the repo owner.
1. Fork the repo.
1. Create a new branch out of the `develop` branch, and give it a meaningful name. A good introduction and best practice to git branching can be found at https://nvie.com/posts/a-successful-git-branching-model/.
1. Hack away in your new branch. Use the issue to discuss your change.
1. When done, create a pull request. Be sure to
    * Refer to the open issue
    * Describe your change
    * Include the name of your branch
1. If all is well your change will be accepted and merge into the original repo.
1. Delete your fork.


###Make a hotfix
1. Check out the running version through its tag
    ```
    git co -b hotfix_7.0.8 v7.0.7`
    ```
1. Fix the problem and verify/test
1. Add the changed files and commit the change
    ```
    git add src/main/kotlin/se/skoview/view/HippoTable.kt
    git ci -m "Implemented hotfix 7.0.8, clear search field after select"
    ```
1. Merge the change to the develop branch (the branch which should be put in production)
    ```
    git co develop
    git merge --no-ff hotfix_7.0.8
    ```
1. Tag the branch
    ```
    git tag -a v7.0.8 -m "Implemented hotfix 7.0.8, clear search field after select"
    ```
1. Test the fix in a local container
    ```
    bin/buildDockerImage.kts -c -r
    ```
1. Push the image to NoGui docker registry
    ```
    bin/buildDockerImage.kts  -p
    ```
1. Start the container in tpinfo-a
    ```
    [larroj@tpinfo-a-web01 bin]$ docker container kill 5df76370cfdc
    [larroj@tpinfo-a-web01 bin]$ ./docker-run-kvfrontend.sh 
    Please provide kvfrontend container tag as parameter
    [   larroj@tpinfo-a-web01 bin]$ ./docker-run-kvfrontend.sh v7.0.8
    ```
1. Verify
1. Merge the hotfix to the current development head branch
    ```
   git merge --no-ff develop 
   Auto-merging src/main/kotlin/se/skoview/view/HippoTable.kt
   CONFLICT (content): Merge conflict in src/main/kotlin/se/skoview/view/HippoTable.kt
   Auto-merging README.md
   CONFLICT (content): Merge conflict in README.md
   Automatic merge failed; fix conflicts and then commit the result.
   ```
In this case, the merge requires manual input.
1. And finally, delete the hotfix branch
    ```
   git br -d hotfix_7.0.8
   ```
### Local testing
Use the *other/run* gradle task to compile, build and start and instance of the application. If there are no errors it will start and listen to port 2000. Use the following URL to access it: http://localhost:2000/ 

###Remote access to devserver
To access the devserver from another computer (Windows) on the same lan.
In webpack.config.d/webpack.js add:

``
config.devServer.host = '0.0.0.0';
``

inside `if (config.devServer)`

## Build production image


###Useful docker commands
```
docker container ls # Lista exekverande containers  
docker container ls -a # Lista alla containers  
docker image rm -f $(docker image ls -q) # Tag bort alla images  
docker pull rojeras/tpinfo-backend:latest # Läs ner image från docker hub  
docker tag rojeras/tpinfo-frontend:latest docker-registry.centrera.se:443/frontend # Tagga imagen för att göra det möjligt att pusha till NGs registry  
docker push docker-registry.centrera.se:443/frontend # Pusha en taggad image till NGs docker registry  
docker pull docker-registry.centrera.se:443/backend # Läs ner imagen från NGs registry (ex till tpinfo 
-servrarna) 
docker build --rm -t back5 . # Tag bort container back5 och återskapa imagen  
docker run --env-file=../backend-envir.lst -p 8081:80 back5 # Kör backend med portar, miljövariabler  
docker run -it back5 /bin/bash # Kör image back5 och ge kontroll till bash i container  
docker exec -it 3d48b2e5d748 /bin/bash # Attach and start bash in a running container  
docker save -o backend-image.tar rojeras/tpinfo-backend:latest # Save an image to a tar file  
docker load -o filename.tar # Load an image from a tar file 
```

## KDoc documentation
Code documentation is provided as KDoc. To access it:
1. Run gradle task *documentation/dokkaHtml*
2. Open the file *build/dokka/index.html* 

