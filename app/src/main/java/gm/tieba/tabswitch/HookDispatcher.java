package gm.tieba.tabswitch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import gm.tieba.tabswitch.hookImpl.CreateView;
import gm.tieba.tabswitch.hookImpl.HomeRecommend;
import gm.tieba.tabswitch.hookImpl.Purify;
import gm.tieba.tabswitch.hookImpl.PurifyEnter;
import gm.tieba.tabswitch.hookImpl.PurifyMy;
import gm.tieba.tabswitch.hookImpl.RedTip;

public class HookDispatcher extends Hook {
    public static void hook(XC_LoadPackage.LoadPackageParam lpparam, Map.Entry<String, ?> entry) throws Throwable {
        try {
            switch (entry.getKey()) {
                case "home_recommend"://写死了被混淆的方法
                    if ((Boolean) entry.getValue()) HomeRecommend.hook(lpparam);
                    break;
                case "enter_forum":
                    if (!(Boolean) entry.getValue()) return;
                    try {
                        XposedHelpers.findAndHookMethod("com.baidu.tieba.flutter.view.FlutterEnterForumDelegateStatic", lpparam.classLoader, "createFragmentTabStructure", XC_MethodReplacement.returnConstant(null));
                    } catch (XposedHelpers.ClassNotFoundError ignored) {
                    }
                    XposedHelpers.findAndHookMethod("com.baidu.tieba.enterForum.home.EnterForumDelegateStatic", lpparam.classLoader, "isAvailable", XC_MethodReplacement.returnConstant(false));
                    break;
                case "new_category":
                    if ((Boolean) entry.getValue())
                        XposedHelpers.findAndHookMethod("com.baidu.tieba.flutter.view.FlutterNewCategoryDelegateStatic", lpparam.classLoader, "isAvailable", XC_MethodReplacement.returnConstant(false));
                    break;
                case "my_message":
                    if ((Boolean) entry.getValue())
                        XposedHelpers.findAndHookMethod("com.baidu.tieba.imMessageCenter.im.chat.notify.ImMessageCenterDelegateStatic", lpparam.classLoader, "isAvailable", XC_MethodReplacement.returnConstant(false));
                    break;
                case "mine":
                    if ((Boolean) entry.getValue())
                        XposedHelpers.findAndHookMethod("com.baidu.tieba.flutter.view.FlutterDelegateStatic", lpparam.classLoader, "isAvailable", XC_MethodReplacement.returnConstant(false));
                    break;
                case "purify":
                    if ((Boolean) entry.getValue()) Purify.hook(lpparam);
                    break;
                case "purify_enter"://写死了被混淆的方法
                    if ((Boolean) entry.getValue()) PurifyEnter.hook(lpparam);
                    break;
                case "purify_my":
                    if ((Boolean) entry.getValue()) PurifyMy.hook(lpparam);
                    break;
                case "red_tip"://写死了被混淆的方法
                    if ((Boolean) entry.getValue()) RedTip.hook(lpparam);
                    break;
                case "follow_filter":
                    if (!(Boolean) entry.getValue()) return;
                    XposedHelpers.findAndHookMethod(XposedHelpers.findClass("tbclient.Personalized.DataRes$Builder", lpparam.classLoader), "build", boolean.class, new XC_MethodHook() {
                        public void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (Hook.follow == null) return;
                            Field field = param.thisObject.getClass().getDeclaredField("thread_list");
                            field.setAccessible(true);
                            List<?> list = (List<?>) field.get(param.thisObject);
                            if (list == null) return;
                            for (int i = 0; i < list.size(); i++) {
                                boolean isRemove = true;
                                for (String pb : Hook.follow)
                                    if (list.get(i).toString().contains("fname=" + pb + ", forum_info="))
                                        isRemove = false;
                                if (isRemove) {
                                    list.remove(i);
                                    i--;
                                }
                            }
                        }
                    });
                    break;
                case "personalized_filter":
                    String personalizedFilter = (String) entry.getValue();
                    if (personalizedFilter == null) return;
                    XposedHelpers.findAndHookMethod(XposedHelpers.findClass("tbclient.Personalized.DataRes$Builder", lpparam.classLoader), "build", boolean.class, new XC_MethodHook() {
                        public void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Field field = param.thisObject.getClass().getDeclaredField("thread_list");
                            field.setAccessible(true);
                            List<?> list = (List<?>) field.get(param.thisObject);
                            if (list == null) return;
                            for (int i = 0; i < list.size(); i++)
                                if (Pattern.compile(personalizedFilter).matcher(list.get(i).toString()).find()) {
                                    if ((Boolean) Hook.preferenceMap.get("personalized_filter_log"))
                                        XposedBridge.log(list.get(i).toString());
                                    list.remove(i);
                                    i--;
                                }
                        }
                    });
                    break;
                case "content_filter":
                    String contentFilter = (String) entry.getValue();
                    if (contentFilter == null) return;
                    XposedHelpers.findAndHookMethod(XposedHelpers.findClass("tbclient.PbPage.DataRes$Builder", lpparam.classLoader), "build", boolean.class, new XC_MethodHook() {
                        public void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Field field = param.thisObject.getClass().getDeclaredField("post_list");
                            field.setAccessible(true);
                            List<?> list = (List<?>) field.get(param.thisObject);
                            if (list == null) return;
                            for (int i = 0; i < list.size(); i++) {
                                String text = list.get(i).toString();
                                text = text.substring(text.indexOf("text=") + 5, text.indexOf(", topic_special_icon="));
                                if (Pattern.compile(contentFilter).matcher(text).find()) {
                                    list.remove(i);
                                    i--;
                                }
                            }
                        }
                    });
                    //楼中楼
                    XposedHelpers.findAndHookMethod(XposedHelpers.findClass("tbclient.SubPostList$Builder", lpparam.classLoader), "build", boolean.class, new XC_MethodHook() {
                        public void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Field field = param.thisObject.getClass().getDeclaredField("content");
                            field.setAccessible(true);
                            List<?> list = (List<?>) field.get(param.thisObject);
                            if (list == null) return;
                            for (int i = 0; i < list.size(); i++) {
                                String text = list.get(i).toString();
                                //出现过1次StringIndexOutOfBoundsException
                                text = text.substring(text.indexOf("text=") + 5, text.indexOf(", topic_special_icon="));
                                if (Pattern.compile(contentFilter).matcher(text).find()) {
                                    list.remove(i);
                                    i--;
                                }
                            }
                        }
                    });
                    break;
                case "create_view":
                    if ((Boolean) entry.getValue()) CreateView.hook(lpparam);
                    break;
                case "auto_sign":
                    if (!(Boolean) entry.getValue()) return;
                    XposedHelpers.findAndHookMethod("com.baidu.tieba.tblauncher.MainTabActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Activity activity = (Activity) param.thisObject;
                            SharedPreferences tsConfig = activity.getSharedPreferences("TS_config", Context.MODE_PRIVATE);
                            SharedPreferences tsCache = activity.getSharedPreferences("TS_cache", Context.MODE_PRIVATE);
                            if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != tsConfig.getInt("sign_date", 0))
                                new Thread(() -> {
                                    Looper.prepare();
                                    if (Hook.BDUSS == null)
                                        Toast.makeText(activity.getApplicationContext(), "暂未获取到 BDUSS", Toast.LENGTH_LONG).show();
                                    else {
                                        String result = top.srcrs.Run.main(Hook.BDUSS);
                                        Toast.makeText(activity.getApplicationContext(), result, Toast.LENGTH_LONG).show();
                                        if (result.endsWith("全部签到成功")) {
                                            SharedPreferences.Editor editConfig = tsConfig.edit();
                                            editConfig.putInt("sign_date", Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
                                            editConfig.apply();
                                            Hook.follow = new HashSet<>(top.srcrs.Run.success);
                                            SharedPreferences.Editor editCache = tsCache.edit();
                                            editCache.putStringSet("follow", Hook.follow);
                                            editCache.apply();
                                        }
                                    }
                                    Looper.loop();
                                }).start();
                        }
                    });
                    break;
                case "open_sign":
                    if (!(Boolean) entry.getValue()) return;
                    XposedHelpers.findAndHookMethod("com.baidu.tieba.tblauncher.MainTabActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Activity activity = (Activity) param.thisObject;
                            SharedPreferences sp = activity.getSharedPreferences("TS_config", Context.MODE_PRIVATE);
                            if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != sp.getInt("sign_date", 0) && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) != 0) {
                                Intent intent = new Intent();
                                intent.setClassName(activity, "com.baidu.tieba.signall.SignAllForumActivity");
                                activity.startActivity(intent);
                            }
                        }
                    });
                    XposedHelpers.findAndHookMethod("com.baidu.tieba.signall.SignAllForumActivity", lpparam.classLoader, "onClick", View.class, new XC_MethodHook() {
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Activity activity = (Activity) param.thisObject;
                            SharedPreferences sp = activity.getSharedPreferences("TS_config", Context.MODE_PRIVATE);
                            if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != sp.getInt("sign_date", 0) && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) != 0) {
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putInt("sign_date", Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
                                editor.apply();
                                activity.finish();
                            }
                        }
                    });
                    break;
                case "font_size":
                    if (!(Boolean) entry.getValue()) return;
                    for (int i = 0; i < ruleMapList.size(); i++) {
                        Map<String, String> map = ruleMapList.get(i);
                        if (Objects.equals(map.get("rule"), "Lcom/baidu/tieba/R$string;->toast_font_size_xlarge:I")) {
                            Method[] methods = lpparam.classLoader.loadClass(map.get("class")).getDeclaredMethods();
                            for (Method method : methods)
                                if (method.getReturnType().toString().equals("void"))
                                    XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(null));
                        }
                    }
                    break;
            }
        } catch (XposedHelpers.ClassNotFoundError e) {
            XposedBridge.log(e);
        }
    }
}