/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
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



class attributeGroupDecl extends NGCCHandler {
    private AnnotationImpl annotation;
    private String name;
    private ForeignAttributesImpl fa;
    protected final NGCCRuntimeEx $runtime;
    private int $_ngcc_current_state;
    protected String $uri;
    protected String $localName;
    protected String $qname;

    public final NGCCRuntime getRuntime() {
        return($runtime);
    }

    public attributeGroupDecl(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie) {
        super(source, parent, cookie);
        $runtime = runtime;
        $_ngcc_current_state = 14;
    }

    public attributeGroupDecl(NGCCRuntimeEx runtime) {
        this(null, runtime, runtime, -1);
    }

    private void action0()throws SAXException {

        result = new AttGroupDeclImpl(
          $runtime.document, annotation, locator, fa, name );

}

    private void action1()throws SAXException {
        locator=$runtime.copyLocator();
}

    public void enterElement(String $__uri, String $__local, String $__qname, Attributes $attrs) throws SAXException {
        int $ai;
        $uri = $__uri;
        $localName = $__local;
        $qname = $__qname;
        switch($_ngcc_current_state) {
        case 7:
            {
                if(($ai = $runtime.getAttributeIndex("","id"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendEnterElement(super._cookie, $__uri, $__local, $__qname, $attrs);
                }
                else {
                    $_ngcc_current_state = 6;
                    $runtime.sendEnterElement(super._cookie, $__uri, $__local, $__qname, $attrs);
                }
            }
            break;
        case 4:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("annotation"))) {
                    NGCCHandler h = new annotation(this, super._source, $runtime, 17, null,AnnotationContext.ATTRIBUTE_GROUP);
                    spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                }
                else {
                    $_ngcc_current_state = 3;
                    $runtime.sendEnterElement(super._cookie, $__uri, $__local, $__qname, $attrs);
                }
            }
            break;
        case 6:
            {
                if((($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("annotation")) || (($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("attribute")) || (($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("attributeGroup")) || ($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("anyAttribute")))))) {
                    NGCCHandler h = new foreignAttributes(this, super._source, $runtime, 19, fa);
                    spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                }
                else {
                    unexpectedEnterElement($__qname);
                }
            }
            break;
        case 14:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("attributeGroup"))) {
                    $runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
                    action1();
                    $_ngcc_current_state = 13;
                }
                else {
                    unexpectedEnterElement($__qname);
                }
            }
            break;
        case 0:
            {
                revertToParentFromEnterElement(result, super._cookie, $__uri, $__local, $__qname, $attrs);
            }
            break;
        case 13:
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
        case 3:
            {
                action0();
                $_ngcc_current_state = 2;
                $runtime.sendEnterElement(super._cookie, $__uri, $__local, $__qname, $attrs);
            }
            break;
        case 2:
            {
                if((($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("attribute")) || (($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("attributeGroup")) || ($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("anyAttribute"))))) {
                    NGCCHandler h = new attributeUses(this, super._source, $runtime, 14, result);
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
        case 7:
            {
                if(($ai = $runtime.getAttributeIndex("","id"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendLeaveElement(super._cookie, $__uri, $__local, $__qname);
                }
                else {
                    $_ngcc_current_state = 6;
                    $runtime.sendLeaveElement(super._cookie, $__uri, $__local, $__qname);
                }
            }
            break;
        case 4:
            {
                $_ngcc_current_state = 3;
                $runtime.sendLeaveElement(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 6:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("attributeGroup"))) {
                    NGCCHandler h = new foreignAttributes(this, super._source, $runtime, 19, fa);
                    spawnChildFromLeaveElement(h, $__uri, $__local, $__qname);
                }
                else {
                    unexpectedLeaveElement($__qname);
                }
            }
            break;
        case 0:
            {
                revertToParentFromLeaveElement(result, super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 13:
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
        case 3:
            {
                action0();
                $_ngcc_current_state = 2;
                $runtime.sendLeaveElement(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 1:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("attributeGroup"))) {
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
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("attributeGroup"))) {
                    NGCCHandler h = new attributeUses(this, super._source, $runtime, 14, result);
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
        case 7:
            {
                if(($__uri.equals("") && $__local.equals("id"))) {
                    $_ngcc_current_state = 9;
                }
                else {
                    $_ngcc_current_state = 6;
                    $runtime.sendEnterAttribute(super._cookie, $__uri, $__local, $__qname);
                }
            }
            break;
        case 4:
            {
                $_ngcc_current_state = 3;
                $runtime.sendEnterAttribute(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 0:
            {
                revertToParentFromEnterAttribute(result, super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 13:
            {
                if(($__uri.equals("") && $__local.equals("name"))) {
                    $_ngcc_current_state = 12;
                }
                else {
                    unexpectedEnterAttribute($__qname);
                }
            }
            break;
        case 3:
            {
                action0();
                $_ngcc_current_state = 2;
                $runtime.sendEnterAttribute(super._cookie, $__uri, $__local, $__qname);
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
        case 8:
            {
                if(($__uri.equals("") && $__local.equals("id"))) {
                    $_ngcc_current_state = 6;
                }
                else {
                    unexpectedLeaveAttribute($__qname);
                }
            }
            break;
        case 7:
            {
                $_ngcc_current_state = 6;
                $runtime.sendLeaveAttribute(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 4:
            {
                $_ngcc_current_state = 3;
                $runtime.sendLeaveAttribute(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 0:
            {
                revertToParentFromLeaveAttribute(result, super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 3:
            {
                action0();
                $_ngcc_current_state = 2;
                $runtime.sendLeaveAttribute(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 11:
            {
                if(($__uri.equals("") && $__local.equals("name"))) {
                    $_ngcc_current_state = 7;
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
        case 7:
            {
                if(($ai = $runtime.getAttributeIndex("","id"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendText(super._cookie, $value);
                }
                else {
                    $_ngcc_current_state = 6;
                    $runtime.sendText(super._cookie, $value);
                }
            }
            break;
        case 4:
            {
                $_ngcc_current_state = 3;
                $runtime.sendText(super._cookie, $value);
            }
            break;
        case 9:
            {
                $_ngcc_current_state = 8;
            }
            break;
        case 12:
            {
                name = $value;
                $_ngcc_current_state = 11;
            }
            break;
        case 0:
            {
                revertToParentFromText(result, super._cookie, $value);
            }
            break;
        case 13:
            {
                if(($ai = $runtime.getAttributeIndex("","name"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendText(super._cookie, $value);
                }
            }
            break;
        case 3:
            {
                action0();
                $_ngcc_current_state = 2;
                $runtime.sendText(super._cookie, $value);
            }
            break;
        }
    }

    public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)throws SAXException {
        switch($__cookie__) {
        case 19:
            {
                fa = ((ForeignAttributesImpl)$__result__);
                $_ngcc_current_state = 4;
            }
            break;
        case 17:
            {
                annotation = ((AnnotationImpl)$__result__);
                $_ngcc_current_state = 3;
            }
            break;
        case 14:
            {
                $_ngcc_current_state = 1;
            }
            break;
        }
    }

    public boolean accepted() {
        return(($_ngcc_current_state == 0));
    }


      private AttGroupDeclImpl result;
      private Locator locator;

}
