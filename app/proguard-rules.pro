-repackageclasses "gm.tieba.tabswitch"
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*,!code/allocation/variable
-keepclassmembers class gm.tieba.tabswitch.ui.MainActivity {public static boolean isModuleActive();}
-keep class gm.tieba.tabswitch.Hook