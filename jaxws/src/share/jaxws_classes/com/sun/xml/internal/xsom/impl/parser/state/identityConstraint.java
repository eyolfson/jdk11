/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

/* this file is generated by RelaxNGCC */
package com.sun.xml.internal.xsom.impl.parser.state;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import com.sun.xml.internal.xsom.impl.parser.NGCCRuntimeEx;

    import com.sun.xml.internal.xsom.*;
    import com.sun.xml.internal.xsom.parser.*;
    import com.sun.xml.internal.xsom.impl.*;
    import com.sun.xml.internal.xsom.impl.parser.*;
    import org.xml.sax.Locator;
    import org.xml.sax.ContentHandler;
    import org.xml.sax.helpers.*;
    import java.util.*;
    import java.math.BigInteger;



class identityConstraint extends NGCCHandler {
    private String name;
    private UName ref;
    private ForeignAttributesImpl fa;
    private AnnotationImpl ann;
    private XPathImpl field;
    protected final NGCCRuntimeEx $runtime;
    private int $_ngcc_current_state;
    protected String $uri;
    protected String $localName;
    protected String $qname;

    public final NGCCRuntime getRuntime() {
        return($runtime);
    }

    public identityConstraint(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie) {
        super(source, parent, cookie);
        $runtime = runtime;
        $_ngcc_current_state = 18;
    }

    public identityConstraint(NGCCRuntimeEx runtime) {
        this(null, runtime, runtime, -1);
    }

    private void action0()throws SAXException {
        fields.add(field);
}

    private void action1()throws SAXException {

            refer = new DelayedRef.IdentityConstraint(
              $runtime, $runtime.copyLocator(), $runtime.currentSchema, ref );

}

    private void action2()throws SAXException {
        if($localName.equals("key"))
        category = XSIdentityConstraint.KEY;
      else
      if($localName.equals("keyref"))
        category = XSIdentityConstraint.KEYREF;
      else
      if($localName.equals("unique"))
        category = XSIdentityConstraint.UNIQUE;
}

    public void enterElement(String $__uri, String $__local, String $__qname, Attributes $attrs) throws SAXException {
        int $ai;
        $uri = $__uri;
        $localName = $__local;
        $qname = $__qname;
        switch($_ngcc_current_state) {
        case 16:
            {
                if(($ai = $runtime.getAttributeIndex("","name"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendEnterElement(super._cookie, $__uri, $__local, $__qname, $attrs);
                }
                else {
                    unexpectedEnterElement($__qname);
                }
            }
            break;
        case 1:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("field"))) {
                    $runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
                    $_ngcc_current_state = 3;
                }
                else {
                    unexpectedEnterElement($__qname);
                }
            }
            break;
        case 0:
            {
                revertToParentFromEnterElement(makeResult(), super._cookie, $__uri, $__local, $__qname, $attrs);
            }
            break;
        case 17:
            {
                if((($ai = $runtime.getAttributeIndex("","name"))>=0 && (($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("selector")) || ($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("annotation"))))) {
                    NGCCHandler h = new foreignAttributes(this, super._source, $runtime, 287, null);
                    spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                }
                else {
                    unexpectedEnterElement($__qname);
                }
            }
            break;
        case 7:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("selector"))) {
                    $runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
                    $_ngcc_current_state = 6;
                }
                else {
                    unexpectedEnterElement($__qname);
                }
            }
            break;
        case 18:
            {
                if(((($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("key")) || ($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("keyref"))) || ($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("unique")))) {
                    $runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
                    action2();
                    $_ngcc_current_state = 17;
                }
                else {
                    unexpectedEnterElement($__qname);
                }
            }
            break;
        case 3:
            {
                if(($ai = $runtime.getAttributeIndex("","xpath"))>=0) {
                    NGCCHandler h = new xpath(this, super._source, $runtime, 270);
                    spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                }
                else {
                    unexpectedEnterElement($__qname);
                }
            }
            break;
        case 4:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("field"))) {
                    $runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
                    $_ngcc_current_state = 3;
                }
                else {
                    unexpectedEnterElement($__qname);
                }
            }
            break;
        case 8:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("annotation"))) {
                    NGCCHandler h = new annotation(this, super._source, $runtime, 277, null,AnnotationContext.IDENTITY_CONSTRAINT);
                    spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                }
                else {
                    $_ngcc_current_state = 7;
                    $runtime.sendEnterElement(super._cookie, $__uri, $__local, $__qname, $attrs);
                }
            }
            break;
        case 10:
            {
                if(($ai = $runtime.getAttributeIndex("","refer"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendEnterElement(super._cookie, $__uri, $__local, $__qname, $attrs);
                }
                else {
                    $_ngcc_current_state = 8;
                    $runtime.sendEnterElement(super._cookie, $__uri, $__local, $__qname, $attrs);
                }
            }
            break;
        case 6:
            {
                if(($ai = $runtime.getAttributeIndex("","xpath"))>=0) {
                    NGCCHandler h = new xpath(this, super._source, $runtime, 274);
                    spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                }
                else {
                    unexpectedEnterElement($__qname);
                }
            }
            break;
        default:
            {
                unexpectedEnterElement($__qname);
            }
            break;
        }
    }

    public void leaveElement(String $__uri, String $__local, String $__qname) throws SAXException {
        int $ai;
        $uri = $__uri;
        $localName = $__local;
        $qname = $__qname;
        switch($_ngcc_current_state) {
        case 5:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("selector"))) {
                    $runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
                    $_ngcc_current_state = 4;
                }
                else {
                    unexpectedLeaveElement($__qname);
                }
            }
            break;
        case 16:
            {
                if(($ai = $runtime.getAttributeIndex("","name"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendLeaveElement(super._cookie, $__uri, $__local, $__qname);
                }
                else {
                    unexpectedLeaveElement($__qname);
                }
            }
            break;
        case 1:
            {
                if(((($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("key")) || ($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("keyref"))) || ($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("unique")))) {
                    $runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
                    $_ngcc_current_state = 0;
                }
                else {
                    unexpectedLeaveElement($__qname);
                }
            }
            break;
        case 2:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("field"))) {
                    $runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
                    $_ngcc_current_state = 1;
                }
                else {
                    unexpectedLeaveElement($__qname);
                }
            }
            break;
        case 0:
            {
                revertToParentFromLeaveElement(makeResult(), super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 17:
            {
                if(($ai = $runtime.getAttributeIndex("","name"))>=0) {
                    NGCCHandler h = new foreignAttributes(this, super._source, $runtime, 287, null);
                    spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
                }
                else {
                    unexpectedLeaveElement($__qname);
                }
            }
            break;
        case 3:
            {
                if((($ai = $runtime.getAttributeIndex("","xpath"))>=0 && ($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("field")))) {
                    NGCCHandler h = new xpath(this, super._source, $runtime, 270);
                    spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
                }
                else {
                    unexpectedLeaveElement($__qname);
                }
            }
            break;
        case 8:
            {
                $_ngcc_current_state = 7;
                $runtime.sendLeaveElement(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 10:
            {
                if(($ai = $runtime.getAttributeIndex("","refer"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendLeaveElement(super._cookie, $__uri, $__local, $__qname);
                }
                else {
                    $_ngcc_current_state = 8;
                    $runtime.sendLeaveElement(super._cookie, $__uri, $__local, $__qname);
                }
            }
            break;
        case 6:
            {
                if((($ai = $runtime.getAttributeIndex("","xpath"))>=0 && ($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("selector")))) {
                    NGCCHandler h = new xpath(this, super._source, $runtime, 274);
                    spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
                }
                else {
                    unexpectedLeaveElement($__qname);
                }
            }
            break;
        default:
            {
                unexpectedLeaveElement($__qname);
            }
            break;
        }
    }

    public void enterAttribute(String $__uri, String $__local, String $__qname) throws SAXException {
        int $ai;
        $uri = $__uri;
        $localName = $__local;
        $qname = $__qname;
        switch($_ngcc_current_state) {
        case 16:
            {
                if(($__uri.equals("") && $__local.equals("name"))) {
                    $_ngcc_current_state = 15;
                }
                else {
                    unexpectedEnterAttribute($__qname);
                }
            }
            break;
        case 0:
            {
                revertToParentFromEnterAttribute(makeResult(), super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 17:
            {
                if(($__uri.equals("") && $__local.equals("name"))) {
                    NGCCHandler h = new foreignAttributes(this, super._source, $runtime, 287, null);
                    spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
                }
                else {
                    unexpectedEnterAttribute($__qname);
                }
            }
            break;
        case 3:
            {
                if(($__uri.equals("") && $__local.equals("xpath"))) {
                    NGCCHandler h = new xpath(this, super._source, $runtime, 270);
                    spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
                }
                else {
                    unexpectedEnterAttribute($__qname);
                }
            }
            break;
        case 8:
            {
                $_ngcc_current_state = 7;
                $runtime.sendEnterAttribute(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 10:
            {
                if(($__uri.equals("") && $__local.equals("refer"))) {
                    $_ngcc_current_state = 12;
                }
                else {
                    $_ngcc_current_state = 8;
                    $runtime.sendEnterAttribute(super._cookie, $__uri, $__local, $__qname);
                }
            }
            break;
        case 6:
            {
                if(($__uri.equals("") && $__local.equals("xpath"))) {
                    NGCCHandler h = new xpath(this, super._source, $runtime, 274);
                    spawnChildFromEnterAttribute(h, $__uri, $__local, $__qname);
                }
                else {
                    unexpectedEnterAttribute($__qname);
                }
            }
            break;
        default:
            {
                unexpectedEnterAttribute($__qname);
            }
            break;
        }
    }

    public void leaveAttribute(String $__uri, String $__local, String $__qname) throws SAXException {
        int $ai;
        $uri = $__uri;
        $localName = $__local;
        $qname = $__qname;
        switch($_ngcc_current_state) {
        case 0:
            {
                revertToParentFromLeaveAttribute(makeResult(), super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 14:
            {
                if(($__uri.equals("") && $__local.equals("name"))) {
                    $_ngcc_current_state = 10;
                }
                else {
                    unexpectedLeaveAttribute($__qname);
                }
            }
            break;
        case 8:
            {
                $_ngcc_current_state = 7;
                $runtime.sendLeaveAttribute(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 10:
            {
                $_ngcc_current_state = 8;
                $runtime.sendLeaveAttribute(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 11:
            {
                if(($__uri.equals("") && $__local.equals("refer"))) {
                    $_ngcc_current_state = 8;
                }
                else {
                    unexpectedLeaveAttribute($__qname);
                }
            }
            break;
        default:
            {
                unexpectedLeaveAttribute($__qname);
            }
            break;
        }
    }

    public void text(String $value) throws SAXException {
        int $ai;
        switch($_ngcc_current_state) {
        case 15:
            {
                name = $value;
                $_ngcc_current_state = 14;
            }
            break;
        case 16:
            {
                if(($ai = $runtime.getAttributeIndex("","name"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendText(super._cookie, $value);
                }
            }
            break;
        case 0:
            {
                revertToParentFromText(makeResult(), super._cookie, $value);
            }
            break;
        case 12:
            {
                NGCCHandler h = new qname(this, super._source, $runtime, 280);
                spawnChildFromText(h, $value);
            }
            break;
        case 17:
            {
                if(($ai = $runtime.getAttributeIndex("","name"))>=0) {
                    NGCCHandler h = new foreignAttributes(this, super._source, $runtime, 287, null);
                    spawnChildFromText(h, $value);
                }
            }
            break;
        case 3:
            {
                if(($ai = $runtime.getAttributeIndex("","xpath"))>=0) {
                    NGCCHandler h = new xpath(this, super._source, $runtime, 270);
                    spawnChildFromText(h, $value);
                }
            }
            break;
        case 8:
            {
                $_ngcc_current_state = 7;
                $runtime.sendText(super._cookie, $value);
            }
            break;
        case 10:
            {
                if(($ai = $runtime.getAttributeIndex("","refer"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendText(super._cookie, $value);
                }
                else {
                    $_ngcc_current_state = 8;
                    $runtime.sendText(super._cookie, $value);
                }
            }
            break;
        case 6:
            {
                if(($ai = $runtime.getAttributeIndex("","xpath"))>=0) {
                    NGCCHandler h = new xpath(this, super._source, $runtime, 274);
                    spawnChildFromText(h, $value);
                }
            }
            break;
        }
    }

    public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)throws SAXException {
        switch($__cookie__) {
        case 270:
            {
                field = ((XPathImpl)$__result__);
                action0();
                $_ngcc_current_state = 2;
            }
            break;
        case 287:
            {
                fa = ((ForeignAttributesImpl)$__result__);
                $_ngcc_current_state = 16;
            }
            break;
        case 280:
            {
                ref = ((UName)$__result__);
                action1();
                $_ngcc_current_state = 11;
            }
            break;
        case 277:
            {
                ann = ((AnnotationImpl)$__result__);
                $_ngcc_current_state = 7;
            }
            break;
        case 274:
            {
                selector = ((XPathImpl)$__result__);
                $_ngcc_current_state = 5;
            }
            break;
        }
    }

    public boolean accepted() {
        return(($_ngcc_current_state == 0));
    }


      private short category;
      private List fields = new ArrayList();
      private XPathImpl selector;
      private DelayedRef.IdentityConstraint refer = null;

      private IdentityConstraintImpl makeResult() {
        return new IdentityConstraintImpl($runtime.document,ann,$runtime.copyLocator(),fa,
          category,name,selector,fields,refer);
      }

}
