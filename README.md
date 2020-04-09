## Migration
### From Kvision 3.0.1 to 3.5.2
1. Clone from bitbucket
1. Checkout develop
1. Branch to a new branch kv3.5.2; `git co -b kv3.5.2 develop`
1. Remove everything except this README.md, `.gitignore` and the `/src` and `/.git` directories
1. Clone down kvision-examples
1. Copy everything from `kvision-examples/showcase` except what has been left in step 3 above.
1. Add the sourceset `implementation("pl.treksoft:kvision-redux-kotlin:$kvisionVersion")` to `build.gradle.kts 
1. Change port from `3000` to `2000 in `build.gradle.kts`
1. Add `import kotlinx.serialization.builtins.*` to `TpdbItems.kt`
1. Open it up in Intellij
1. Build, run and verify
1. Do a `git add` and `commit` of all changed files


## Gradle Tasks
Whenever you want to produce a minified "production" version of your code pass in `-Pproduction=true` or `-Pprod=true` to your build command.
### Resource Processing
* generatePotFile - Generates a `src/main/resources/i18n/messages.pot` translation template file.
### Running
* run - Starts a webpack dev server on port 2000.
### Packaging
* browserWebpack - Bundles the compiled js files into `build/distributions`
* zip - Packages a zip archive with all required files into `build/libs/*.zip`
<<<<<<< HEAD
## Tips from Robert
I personally like to use client side routing as a main application entry point with state being hold in one or more Redux stores and views generated with KVision StateBinding components.

But I don't think any of the examples are built this way - it's better suited for bigger apps.

And I'm building server side apps (with Spring Boot), so I mostly use KVision server side interfaces.

So the flow is mostly about something like this:
1. URL in the browser (with client side route parameters)
2. Client side router executed, extracting parameters
3. Call one or more remote methods with these parameters
4. Process business logic on the server side and return data for the client
5. Dispatch returned data to the Redux store
6. Automatically rebuild view with this data

    With bigger apps I use StackPanel to separate different parts of the application (different urls activate different stack children, showing the correct view)


##Hotfix
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