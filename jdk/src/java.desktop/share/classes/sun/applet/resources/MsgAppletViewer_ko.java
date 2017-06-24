/*
 * Copyright (c) 1996, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package sun.applet.resources;

import java.util.ListResourceBundle;

public class MsgAppletViewer_ko extends ListResourceBundle {

    public Object[][] getContents() {
        Object[][] temp = new Object[][] {
            {"textframe.button.dismiss", "\uD574\uC81C"},
            {"appletviewer.tool.title", "\uC560\uD50C\uB9BF \uBDF0\uC5B4: {0}"},
            {"appletviewer.menu.applet", "\uC560\uD50C\uB9BF"},
            {"appletviewer.menuitem.restart", "\uC7AC\uC2DC\uC791"},
            {"appletviewer.menuitem.reload", "\uC7AC\uB85C\uB4DC"},
            {"appletviewer.menuitem.stop", "\uC815\uC9C0"},
            {"appletviewer.menuitem.save", "\uC800\uC7A5..."},
            {"appletviewer.menuitem.start", "\uC2DC\uC791"},
            {"appletviewer.menuitem.clone", "\uBCF5\uC81C..."},
            {"appletviewer.menuitem.tag", "\uD0DC\uADF8 \uC9C0\uC815..."},
            {"appletviewer.menuitem.info", "\uC815\uBCF4..."},
            {"appletviewer.menuitem.edit", "\uD3B8\uC9D1"},
            {"appletviewer.menuitem.encoding", "\uBB38\uC790 \uC778\uCF54\uB529"},
            {"appletviewer.menuitem.print", "\uC778\uC1C4..."},
            {"appletviewer.menuitem.props", "\uC18D\uC131..."},
            {"appletviewer.menuitem.close", "\uB2EB\uAE30"},
            {"appletviewer.menuitem.quit", "\uC885\uB8CC"},
            {"appletviewer.label.hello", "\uC2DC\uC791..."},
            {"appletviewer.status.start", "\uC560\uD50C\uB9BF\uC744 \uC2DC\uC791\uD558\uB294 \uC911..."},
            {"appletviewer.appletsave.filedialogtitle","\uD30C\uC77C\uB85C \uC560\uD50C\uB9BF \uC9C1\uB82C\uD654"},
            {"appletviewer.appletsave.err1", "{0}\uC744(\uB97C) {1}(\uC73C)\uB85C \uC9C1\uB82C\uD654\uD558\uB294 \uC911"},
            {"appletviewer.appletsave.err2", "appletSave\uC5D0 \uC624\uB958 \uBC1C\uC0DD: {0}"},
            {"appletviewer.applettag", "\uD0DC\uADF8\uAC00 \uD45C\uC2DC\uB428"},
            {"appletviewer.applettag.textframe", "\uC560\uD50C\uB9BF HTML \uD0DC\uADF8"},
            {"appletviewer.appletinfo.applet", "-- \uC560\uD50C\uB9BF \uC815\uBCF4 \uC5C6\uC74C --"},
            {"appletviewer.appletinfo.param", "-- \uB9E4\uAC1C\uBCC0\uC218 \uC815\uBCF4 \uC5C6\uC74C --"},
            {"appletviewer.appletinfo.textframe", "\uC560\uD50C\uB9BF \uC815\uBCF4"},
            {"appletviewer.appletprint.fail", "\uC778\uC1C4\uB97C \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4."},
            {"appletviewer.appletprint.finish", "\uC778\uC1C4\uB97C \uC644\uB8CC\uD588\uC2B5\uB2C8\uB2E4."},
            {"appletviewer.appletprint.cancel", "\uC778\uC1C4\uAC00 \uCDE8\uC18C\uB418\uC5C8\uC2B5\uB2C8\uB2E4."},
            {"appletviewer.appletencoding", "\uBB38\uC790 \uC778\uCF54\uB529: {0}"},
            {"appletviewer.parse.warning.requiresname", "\uACBD\uACE0: <param name=... value=...> \uD0DC\uADF8\uC5D0\uB294 name \uC18D\uC131\uC774 \uD544\uC694\uD569\uB2C8\uB2E4."},
            {"appletviewer.parse.warning.paramoutside", "\uACBD\uACE0: <param> \uD0DC\uADF8\uAC00 <applet> ... </applet> \uBC16\uC5D0 \uC788\uC2B5\uB2C8\uB2E4."},
            {"appletviewer.parse.warning.applet.requirescode", "\uACBD\uACE0: <applet> \uD0DC\uADF8\uC5D0\uB294 code \uC18D\uC131\uC774 \uD544\uC694\uD569\uB2C8\uB2E4."},
            {"appletviewer.parse.warning.applet.requiresheight", "\uACBD\uACE0: <applet> \uD0DC\uADF8\uC5D0\uB294 height \uC18D\uC131\uC774 \uD544\uC694\uD569\uB2C8\uB2E4."},
            {"appletviewer.parse.warning.applet.requireswidth", "\uACBD\uACE0: <applet> \uD0DC\uADF8\uC5D0\uB294 width \uC18D\uC131\uC774 \uD544\uC694\uD569\uB2C8\uB2E4."},
            {"appletviewer.parse.warning.object.requirescode", "\uACBD\uACE0: <object> \uD0DC\uADF8\uC5D0\uB294 code \uC18D\uC131\uC774 \uD544\uC694\uD569\uB2C8\uB2E4."},
            {"appletviewer.parse.warning.object.requiresheight", "\uACBD\uACE0: <object> \uD0DC\uADF8\uC5D0\uB294 height \uC18D\uC131\uC774 \uD544\uC694\uD569\uB2C8\uB2E4."},
            {"appletviewer.parse.warning.object.requireswidth", "\uACBD\uACE0: <object> \uD0DC\uADF8\uC5D0\uB294 width \uC18D\uC131\uC774 \uD544\uC694\uD569\uB2C8\uB2E4."},
            {"appletviewer.parse.warning.embed.requirescode", "\uACBD\uACE0: <embed> \uD0DC\uADF8\uC5D0\uB294 code \uC18D\uC131\uC774 \uD544\uC694\uD569\uB2C8\uB2E4."},
            {"appletviewer.parse.warning.embed.requiresheight", "\uACBD\uACE0: <embed> \uD0DC\uADF8\uC5D0\uB294 height \uC18D\uC131\uC774 \uD544\uC694\uD569\uB2C8\uB2E4."},
            {"appletviewer.parse.warning.embed.requireswidth", "\uACBD\uACE0: <embed> \uD0DC\uADF8\uC5D0\uB294 width \uC18D\uC131\uC774 \uD544\uC694\uD569\uB2C8\uB2E4."},
            {"appletviewer.parse.warning.appnotLongersupported", "\uACBD\uACE0: <app> \uD0DC\uADF8\uB294 \uB354 \uC774\uC0C1 \uC9C0\uC6D0\uB418\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4. \uB300\uC2E0 <applet>\uC744 \uC0AC\uC6A9\uD558\uC2ED\uC2DC\uC624."},
            {"appletviewer.deprecated", "\uACBD\uACE0: \uC560\uD50C\uB9BF API \uBC0F AppletViewer\uAC00 \uC0AC\uC6A9\uB418\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4."},
            {"appletviewer.usage", "\uC0AC\uC6A9\uBC95: appletviewer <options> url(s)\n\n\uC5EC\uAE30\uC11C <options>\uB294 \uB2E4\uC74C\uACFC \uAC19\uC2B5\uB2C8\uB2E4.\n  -encoding <encoding>    HTML \uD30C\uC77C\uC5D0 \uC0AC\uC6A9\uB420 \uBB38\uC790 \uC778\uCF54\uB529\uC744 \uC9C0\uC815\uD569\uB2C8\uB2E4.\n  -J<runtime flag>        Java \uC778\uD130\uD504\uB9AC\uD130\uB85C \uC778\uC218\uB97C \uC804\uB2EC\uD569\uB2C8\uB2E4.\n\n-J \uC635\uC158\uC740 \uD45C\uC900\uC774 \uC544\uB2C8\uBA70 \uC608\uACE0 \uC5C6\uC774 \uBCC0\uACBD\uB420 \uC218 \uC788\uC2B5\uB2C8\uB2E4."},
            {"appletviewer.main.err.unsupportedopt", "\uC9C0\uC6D0\uB418\uC9C0 \uC54A\uB294 \uC635\uC158: {0}"},
            {"appletviewer.main.err.unrecognizedarg", "\uC54C \uC218 \uC5C6\uB294 \uC778\uC218: {0}"},
            {"appletviewer.main.err.dupoption", "\uC911\uBCF5\uB41C \uC635\uC158 \uC0AC\uC6A9: {0}"},
            {"appletviewer.main.err.inputfile", "\uC9C0\uC815\uB41C \uC785\uB825 \uD30C\uC77C\uC774 \uC5C6\uC2B5\uB2C8\uB2E4."},
            {"appletviewer.main.err.badurl", "\uC798\uBABB\uB41C URL: {0}({1})"},
            {"appletviewer.main.err.io", "\uC77D\uB294 \uC911 I/O \uC608\uC678\uC0AC\uD56D \uBC1C\uC0DD: {0}"},
            {"appletviewer.main.err.readablefile", "{0}\uC774(\uAC00) \uD30C\uC77C\uC774\uBA70 \uC77D\uAE30 \uAC00\uB2A5\uD55C \uC0C1\uD0DC\uC778\uC9C0 \uD655\uC778\uD558\uC2ED\uC2DC\uC624."},
            {"appletviewer.main.err.correcturl", "{0}\uC774(\uAC00) \uC62C\uBC14\uB978 URL\uC785\uB2C8\uAE4C?"},
            {"appletviewer.main.prop.store", "\uC0AC\uC6A9\uC790 \uAD00\uB828 AppletViewer \uC18D\uC131"},
            {"appletviewer.main.err.prop.cantread", "\uC0AC\uC6A9\uC790 \uC18D\uC131 \uD30C\uC77C\uC744 \uC77D\uC744 \uC218 \uC5C6\uC74C: {0}"},
            {"appletviewer.main.err.prop.cantsave", "\uC0AC\uC6A9\uC790 \uC18D\uC131 \uD30C\uC77C\uC744 \uC800\uC7A5\uD560 \uC218 \uC5C6\uC74C: {0}"},
            {"appletviewer.main.warn.nosecmgr", "\uACBD\uACE0: \uBCF4\uC548\uC744 \uC0AC\uC6A9 \uC548\uD568\uC73C\uB85C \uC124\uC815\uD558\uB294 \uC911\uC785\uB2C8\uB2E4."},
            {"appletviewer.main.debug.cantfinddebug", "\uB514\uBC84\uAC70\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4!"},
            {"appletviewer.main.debug.cantfindmain", "\uB514\uBC84\uAC70\uC5D0\uC11C \uAE30\uBCF8 \uBA54\uC18C\uB4DC\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4!"},
            {"appletviewer.main.debug.exceptionindebug", "\uB514\uBC84\uAC70\uC5D0 \uC608\uC678\uC0AC\uD56D\uC774 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4!"},
            {"appletviewer.main.debug.cantaccess", "\uB514\uBC84\uAC70\uC5D0 \uC561\uC138\uC2A4\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4!"},
            {"appletviewer.main.nosecmgr", "\uACBD\uACE0: SecurityManager\uAC00 \uC124\uCE58\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4!"},
            {"appletviewer.main.warning", "\uACBD\uACE0: \uC2DC\uC791\uB41C \uC560\uD50C\uB9BF\uC774 \uC5C6\uC2B5\uB2C8\uB2E4. <applet> \uD0DC\uADF8\uAC00 \uC785\uB825\uB418\uC5C8\uB294\uC9C0 \uD655\uC778\uD558\uC2ED\uC2DC\uC624."},
            {"appletviewer.main.warn.prop.overwrite", "\uACBD\uACE0: \uC0AC\uC6A9\uC790\uC758 \uC694\uCCAD\uC5D0 \uB530\uB77C \uC77C\uC2DC\uC801\uC73C\uB85C \uC2DC\uC2A4\uD15C \uC18D\uC131\uC744 \uACB9\uCCD0 \uC4F0\uB294 \uC911: \uD0A4: {0}, \uC774\uC804 \uAC12: {1}, \uC0C8 \uAC12: {2}"},
            {"appletviewer.main.warn.cantreadprops", "\uACBD\uACE0: AppletViewer \uC18D\uC131 \uD30C\uC77C\uC744 \uC77D\uC744 \uC218 \uC5C6\uC74C: {0}. \uAE30\uBCF8\uAC12\uC744 \uC0AC\uC6A9\uD558\uB294 \uC911\uC785\uB2C8\uB2E4."},
            {"appletioexception.loadclass.throw.interrupted", "\uD074\uB798\uC2A4 \uB85C\uB4DC\uAC00 \uC911\uB2E8\uB428: {0}"},
            {"appletioexception.loadclass.throw.notloaded", "\uD074\uB798\uC2A4\uAC00 \uB85C\uB4DC\uB418\uC9C0 \uC54A\uC74C: {0}"},
            {"appletclassloader.loadcode.verbose", "{1}\uC744(\uB97C) \uAC00\uC838\uC624\uAE30 \uC704\uD574 {0}\uC5D0 \uB300\uD55C \uC2A4\uD2B8\uB9BC\uC744 \uC5EC\uB294 \uC911"},
            {"appletclassloader.filenotfound", "{0}\uC744(\uB97C) \uAC80\uC0C9\uD558\uB294 \uC911 \uD30C\uC77C\uC744 \uCC3E\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4."},
            {"appletclassloader.fileformat", "\uB85C\uB4DC \uC911 \uD30C\uC77C \uD615\uC2DD \uC608\uC678\uC0AC\uD56D \uBC1C\uC0DD: {0}"},
            {"appletclassloader.fileioexception", "\uB85C\uB4DC \uC911 I/O \uC608\uC678\uC0AC\uD56D \uBC1C\uC0DD: {0}"},
            {"appletclassloader.fileexception", "\uB85C\uB4DC \uC911 {0} \uC608\uC678\uC0AC\uD56D \uBC1C\uC0DD: {1}"},
            {"appletclassloader.filedeath", "\uB85C\uB4DC \uC911 {0}\uC774(\uAC00) \uC885\uB8CC\uB428: {1}"},
            {"appletclassloader.fileerror", "\uB85C\uB4DC \uC911 {0} \uC624\uB958 \uBC1C\uC0DD: {1}"},
            {"appletclassloader.findclass.verbose.openstream", "{1}\uC744(\uB97C) \uAC00\uC838\uC624\uAE30 \uC704\uD574 {0}\uC5D0 \uB300\uD55C \uC2A4\uD2B8\uB9BC\uC744 \uC5EC\uB294 \uC911"},
            {"appletclassloader.getresource.verbose.forname", "\uC774\uB984\uC5D0 \uB300\uD55C AppletClassLoader.getResource: {0}"},
            {"appletclassloader.getresource.verbose.found", "\uC2DC\uC2A4\uD15C \uB9AC\uC18C\uC2A4\uB85C {0} \uB9AC\uC18C\uC2A4\uB97C \uCC3E\uC558\uC2B5\uB2C8\uB2E4."},
            {"appletclassloader.getresourceasstream.verbose", "\uC2DC\uC2A4\uD15C \uB9AC\uC18C\uC2A4\uB85C {0} \uB9AC\uC18C\uC2A4\uB97C \uCC3E\uC558\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.runloader.err", "\uAC1D\uCCB4 \uB610\uB294 \uCF54\uB4DC \uB9E4\uAC1C\uBCC0\uC218\uC785\uB2C8\uB2E4!"},
            {"appletpanel.runloader.exception", "{0}\uC758 \uC9C1\uB82C\uD654\uB97C \uD574\uC81C\uD558\uB294 \uC911 \uC608\uC678\uC0AC\uD56D\uC774 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.destroyed", "\uC560\uD50C\uB9BF\uC774 \uC0AD\uC81C\uB418\uC5C8\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.loaded", "\uC560\uD50C\uB9BF\uC774 \uB85C\uB4DC\uB418\uC5C8\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.started", "\uC560\uD50C\uB9BF\uC774 \uC2DC\uC791\uB418\uC5C8\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.inited", "\uC560\uD50C\uB9BF\uC774 \uCD08\uAE30\uD654\uB418\uC5C8\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.stopped", "\uC560\uD50C\uB9BF\uC774 \uC815\uC9C0\uB418\uC5C8\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.disposed", "\uC560\uD50C\uB9BF\uC774 \uBC30\uCE58\uB418\uC5C8\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.nocode", "APPLET \uD0DC\uADF8\uC5D0 CODE \uB9E4\uAC1C\uBCC0\uC218\uAC00 \uB204\uB77D\uB418\uC5C8\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.notfound", "\uB85C\uB4DC: {0} \uD074\uB798\uC2A4\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.nocreate", "\uB85C\uB4DC: {0}\uC744(\uB97C) \uC778\uC2A4\uD134\uC2A4\uD654\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.noconstruct", "\uB85C\uB4DC: {0}\uC740(\uB294) \uACF5\uC6A9\uC774 \uC544\uB2C8\uAC70\uB098 \uACF5\uC6A9 \uC0DD\uC131\uC790\uB97C \uD3EC\uD568\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.death", "\uC885\uB8CC\uB428"},
            {"appletpanel.exception", "\uC608\uC678\uC0AC\uD56D: {0}."},
            {"appletpanel.exception2", "\uC608\uC678\uC0AC\uD56D: {0}: {1}."},
            {"appletpanel.error", "\uC624\uB958: {0}."},
            {"appletpanel.error2", "\uC624\uB958: {0}: {1}."},
            {"appletpanel.notloaded", "\uCD08\uAE30\uD654: \uC560\uD50C\uB9BF\uC774 \uB85C\uB4DC\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.notinited", "\uC2DC\uC791: \uC560\uD50C\uB9BF\uC774 \uCD08\uAE30\uD654\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.notstarted", "\uC815\uC9C0: \uC560\uD50C\uB9BF\uC774 \uC2DC\uC791\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.notstopped", "\uC0AD\uC81C: \uC560\uD50C\uB9BF\uC774 \uC815\uC9C0\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.notdestroyed", "\uBC30\uCE58: \uC560\uD50C\uB9BF\uC774 \uC0AD\uC81C\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.notdisposed", "\uB85C\uB4DC: \uC560\uD50C\uB9BF\uC774 \uBC30\uCE58\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.bail", "\uC911\uB2E8\uB428: \uC911\uB2E8\uD558\uB294 \uC911\uC785\uB2C8\uB2E4."},
            {"appletpanel.filenotfound", "{0}\uC744(\uB97C) \uAC80\uC0C9\uD558\uB294 \uC911 \uD30C\uC77C\uC744 \uCC3E\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4."},
            {"appletpanel.fileformat", "\uB85C\uB4DC \uC911 \uD30C\uC77C \uD615\uC2DD \uC608\uC678\uC0AC\uD56D \uBC1C\uC0DD: {0}"},
            {"appletpanel.fileioexception", "\uB85C\uB4DC \uC911 I/O \uC608\uC678\uC0AC\uD56D \uBC1C\uC0DD: {0}"},
            {"appletpanel.fileexception", "\uB85C\uB4DC \uC911 {0} \uC608\uC678\uC0AC\uD56D \uBC1C\uC0DD: {1}"},
            {"appletpanel.filedeath", "\uB85C\uB4DC \uC911 {0}\uC774(\uAC00) \uC885\uB8CC\uB428: {1}"},
            {"appletpanel.fileerror", "\uB85C\uB4DC \uC911 {0} \uC624\uB958 \uBC1C\uC0DD: {1}"},
            {"appletpanel.badattribute.exception", "HTML \uAD6C\uBB38\uBD84\uC11D \uC911: width/height \uC18D\uC131\uC5D0 \uB300\uD55C \uAC12\uC774 \uC62C\uBC14\uB974\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4."},
            {"appletillegalargumentexception.objectinputstream", "AppletObjectInputStream\uC5D0 \uB110\uC774 \uC544\uB2CC \uB85C\uB354\uAC00 \uD544\uC694\uD569\uB2C8\uB2E4."},
            {"appletprops.title", "AppletViewer \uC18D\uC131"},
            {"appletprops.label.http.server", "HTTP \uD504\uB85D\uC2DC \uC11C\uBC84:"},
            {"appletprops.label.http.proxy", "HTTP \uD504\uB85D\uC2DC \uD3EC\uD2B8:"},
            {"appletprops.label.network", "\uB124\uD2B8\uC6CC\uD06C \uC561\uC138\uC2A4:"},
            {"appletprops.choice.network.item.none", "\uC5C6\uC74C"},
            {"appletprops.choice.network.item.applethost", "\uC560\uD50C\uB9BF \uD638\uC2A4\uD2B8"},
            {"appletprops.choice.network.item.unrestricted", "\uC81C\uD55C\uB418\uC9C0 \uC54A\uC74C"},
            {"appletprops.label.class", "\uD074\uB798\uC2A4 \uC561\uC138\uC2A4:"},
            {"appletprops.choice.class.item.restricted", "\uC81C\uD55C\uB428"},
            {"appletprops.choice.class.item.unrestricted", "\uC81C\uD55C\uB418\uC9C0 \uC54A\uC74C"},
            {"appletprops.label.unsignedapplet", "\uC11C\uBA85\uB418\uC9C0 \uC54A\uC740 \uC560\uD50C\uB9BF \uD5C8\uC6A9:"},
            {"appletprops.choice.unsignedapplet.no", "\uC544\uB2C8\uC624"},
            {"appletprops.choice.unsignedapplet.yes", "\uC608"},
            {"appletprops.button.apply", "\uC801\uC6A9"},
            {"appletprops.button.cancel", "\uCDE8\uC18C"},
            {"appletprops.button.reset", "\uC7AC\uC124\uC815"},
            {"appletprops.apply.exception", "\uC18D\uC131 \uC800\uC7A5 \uC2E4\uD328: {0}"},
            /* 4066432 */
            {"appletprops.title.invalidproxy", "\uBD80\uC801\uD569\uD55C \uD56D\uBAA9"},
            {"appletprops.label.invalidproxy", "\uD504\uB85D\uC2DC \uD3EC\uD2B8\uB294 \uC591\uC758 \uC815\uC218 \uAC12\uC774\uC5B4\uC57C \uD569\uB2C8\uB2E4."},
            {"appletprops.button.ok", "\uD655\uC778"},
            /* end 4066432 */
            {"appletprops.prop.store", "\uC0AC\uC6A9\uC790 \uAD00\uB828 AppletViewer \uC18D\uC131"},
            {"appletsecurityexception.checkcreateclassloader", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uD074\uB798\uC2A4 \uB85C\uB354"},
            {"appletsecurityexception.checkaccess.thread", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uC2A4\uB808\uB4DC"},
            {"appletsecurityexception.checkaccess.threadgroup", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uC2A4\uB808\uB4DC \uADF8\uB8F9: {0}"},
            {"appletsecurityexception.checkexit", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uC885\uB8CC: {0}"},
            {"appletsecurityexception.checkexec", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uC2E4\uD589: {0}"},
            {"appletsecurityexception.checklink", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uB9C1\uD06C: {0}"},
            {"appletsecurityexception.checkpropsaccess", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uC18D\uC131"},
            {"appletsecurityexception.checkpropsaccess.key", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uC18D\uC131 \uC561\uC138\uC2A4 {0}"},
            {"appletsecurityexception.checkread.exception1", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: {0}, {1}"},
            {"appletsecurityexception.checkread.exception2", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: file.read: {0}"},
            {"appletsecurityexception.checkread", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: file.read: {0} == {1}"},
            {"appletsecurityexception.checkwrite.exception", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: {0}, {1}"},
            {"appletsecurityexception.checkwrite", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: file.write: {0} == {1}"},
            {"appletsecurityexception.checkread.fd", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: fd.read"},
            {"appletsecurityexception.checkwrite.fd", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: fd.write"},
            {"appletsecurityexception.checklisten", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: socket.listen: {0}"},
            {"appletsecurityexception.checkaccept", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: socket.accept: {0}:{1}"},
            {"appletsecurityexception.checkconnect.networknone", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: socket.connect: {0}->{1}"},
            {"appletsecurityexception.checkconnect.networkhost1", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: {1}\uC5D0\uC11C {0}\uC5D0 \uC811\uC18D\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4."},
            {"appletsecurityexception.checkconnect.networkhost2", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: {0} \uD638\uC2A4\uD2B8 \uB610\uB294 {1}\uC5D0 \uB300\uD55C IP\uB97C \uBD84\uC11D\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4. "},
            {"appletsecurityexception.checkconnect.networkhost3", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: {0} \uD638\uC2A4\uD2B8\uC5D0 \uB300\uD55C IP\uB97C \uBD84\uC11D\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4. trustProxy \uC18D\uC131\uC744 \uCC38\uC870\uD558\uC2ED\uC2DC\uC624."},
            {"appletsecurityexception.checkconnect", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uC811\uC18D: {0}->{1}"},
            {"appletsecurityexception.checkpackageaccess", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uD328\uD0A4\uC9C0\uC5D0 \uC561\uC138\uC2A4\uD560 \uC218 \uC5C6\uC74C: {0}"},
            {"appletsecurityexception.checkpackagedefinition", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uD328\uD0A4\uC9C0\uB97C \uC815\uC758\uD560 \uC218 \uC5C6\uC74C: {0}"},
            {"appletsecurityexception.cannotsetfactory", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uD329\uD1A0\uB9AC\uB97C \uC124\uC815\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4."},
            {"appletsecurityexception.checkmemberaccess", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uBA64\uBC84 \uC561\uC138\uC2A4\uB97C \uD655\uC778\uD558\uC2ED\uC2DC\uC624."},
            {"appletsecurityexception.checkgetprintjob", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: getPrintJob"},
            {"appletsecurityexception.checksystemclipboardaccess", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: getSystemClipboard"},
            {"appletsecurityexception.checkawteventqueueaccess", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: getEventQueue"},
            {"appletsecurityexception.checksecurityaccess", "\uBCF4\uC548 \uC608\uC678\uC0AC\uD56D: \uBCF4\uC548 \uC791\uC5C5: {0}"},
            {"appletsecurityexception.getsecuritycontext.unknown", "\uC54C \uC218 \uC5C6\uB294 \uD074\uB798\uC2A4 \uB85C\uB354 \uC720\uD615\uC785\uB2C8\uB2E4. getContext\uB97C \uD655\uC778\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4."},
            {"appletsecurityexception.checkread.unknown", "\uC54C \uC218 \uC5C6\uB294 \uD074\uB798\uC2A4 \uB85C\uB354 \uC720\uD615\uC785\uB2C8\uB2E4. {0} \uC77D\uAE30\uB97C \uD655\uC778\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4."},
            {"appletsecurityexception.checkconnect.unknown", "\uC54C \uC218 \uC5C6\uB294 \uD074\uB798\uC2A4 \uB85C\uB354 \uC720\uD615\uC785\uB2C8\uB2E4. \uC811\uC18D\uC744 \uD655\uC778\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4."},
        };

        return temp;
    }
}
