To re-generate Ejs from the root do the following:

1.- Open the Ejs Workspace with Eclipse.
2.- Clean and rebuild both the Ejs and the OSP_core projects.
3.- Right-click the OSP_core file osp_core.jardesc and select "Create JAR". 
    Accept all messages that appear. This will cause a recompilation.
4.- Run the class org.colos.ejs.PackageEjs
    The console will show some messages concerning missing files. This is pretty normal. I use these messages for debugging purposes.
5.- Run the class org.colos.ejs.CreateDistributionEjs and you'll get a new Ejs.

Francisco Esquembre
Universidad de Murcia
SPAIN

http://www.um.es/fem/Ejs
October 2008