<div align="center">

<> Some kind of application for background work <>

<img src="src/main/resources/backwork_preview_2.jpg" alt="" title="" />
<img src="src/main/resources/backwork_preview_1.jpg" alt="" title="" />

### Main Technical Stack

Java 25  
Spring Boot 4    
Maven  
MongoDB  
Java Swing
</div>

### Implementation feature

<div align="left">

#### Main

Back-end
> - CRUD Operation with MongoDB database
> - DTO (Data Transfer Object) Usage because of date and ids
> - Swagger integrated
> - Scheduling (with Spring)
> - Logging with Logback

GUI
> - Swing
> - Desktop notifications
> - 4 Tabs
    2 Console, 1 is for debugs
> - Panel of buttons for various logic
> - Task Panel
    - Add Task (popupdialog), hide-unhide completed, SFX on task completion,
    - Separated normal tasks and recurring tasks
    - Edit and delete tasks
    - Select Task and apply a batch action
    - onHover behavior for component and their child
    - Display time remaining, loop interval, and indicator

</div>

#### Deprecated

-

#### Comments

- Using Java Swing because I want a ligthweight GUI
- Trying to use native Java as much as possible and be less reliant on external frameworks/dependencies
- Need : lightweight solution to watch youtube video in backgrounds
    - > Swing doesn't have webview nor solution for yt videos, Introducing JFXPanel (Java FX) is against current
      philosophy of being lightweight
    - > JCEF (Java Chromium Embedded Framework) Webview but heavy on RAM
    - > Current solution : external browser

#### Plan

*

### Others

*

### Custom Launcher Setup

Context : Not running MongoDB on start for RAM efficiency/saving, not launching twice the same application

So i need to start it before starting Backwork with :  
`net start MongoDB`
But it requires Admin privilege

but on giving the .bat run on administrator I have to confirm Admin usage each time I launch it
<img src="src/main/resources/static/cmd_admin_access_confirm.png" alt="" title="" />

So I go through Windows Task Planifier, create a task that run this .bat script
And use a shortcut to this windows Task to launch the script in admin privilege without having to confirm it each time
The shortcut has this command :
``schtasks.exe /run /tn "Lance Backwork-MongoDB" ``

Yes it save a click, but this setup also save RAM by not having mongodb running everytime

