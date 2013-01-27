/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

package javax.lang.model.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.annotation.IncompleteAnnotationException;
import java.util.List;
import javax.lang.model.element.*;
import javax.lang.model.type.*;

/**
 * Utility methods for operating on types.
 *
 * <p><b>Compatibility Note:</b> Methods may be added to this interface
 * in future releases of the platform.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @author Peter von der Ah&eacute;
 * @see javax.annotation.processing.ProcessingEnvironment#getTypeUtils
 * @since 1.6
 */
public interface Types {

    /**
     * Returns the element corresponding to a type.
     * The type may be a {@code DeclaredType} or {@code TypeVariable}.
     * Returns {@code null} if the type is not one with a
     * corresponding element.
     *
     * @return the element corresponding to the given type
     */
    Element asElement(TypeMirror t);

    /**
     * Tests whether two {@code TypeMirror} objects represent the same type.
     *
     * <p>Caveat: if either of the arguments to this method represents a
     * wildcard, this method will return false.  As a consequence, a wildcard
     * is not the same type as itself.  This might be surprising at first,
     * but makes sense once you consider that an example like this must be
     * rejected by the compiler:
     * <pre>
     *   {@code List<?> list = new ArrayList<Object>();}
     *   {@code list.add(list.get(0));}
     * </pre>
     *
     * @param t1  the first type
     * @param t2  the second type
     * @return {@code true} if and only if the two types are the same
     */
    boolean isSameType(TypeMirror t1, TypeMirror t2);

    /**
     * Tests whether one type is a subtype of another.
     * Any type is considered to be a subtype of itself.
     *
     * @param t1  the first type
     * @param t2  the second type
     * @return {@code true} if and only if the first type is a subtype
     *          of the second
     * @throws IllegalArgumentException if given an executable or package type
     * @jls 4.10 Subtyping
     */
    boolean isSubtype(TypeMirror t1, TypeMirror t2);

    /**
     * Tests whether one type is assignable to another.
     *
     * @param t1  the first type
     * @param t2  the second type
     * @return {@code true} if and only if the first type is assignable
     *          to the second
     * @throws IllegalArgumentException if given an executable or package type
     * @jls 5.2 Assignment Conversion
     */
    boolean isAssignable(TypeMirror t1, TypeMirror t2);

    /**
     * Tests whether one type argument <i>contains</i> another.
     *
     * @param t1  the first type
     * @param t2  the second type
     * @return {@code true} if and only if the first type contains the second
     * @throws IllegalArgumentException if given an executable or package type
     * @jls 4.5.1.1 Type Argument Containment and Equivalence
     */
    boolean contains(TypeMirror t1, TypeMirror t2);

    /**
     * Tests whether the signature of one method is a <i>subsignature</i>
     * of another.
     *
     * @param m1  the first method
     * @param m2  the second method
     * @return {@code true} if and only if the first signature is a
     *          subsignature of the second
     * @jls 8.4.2 Method Signature
     */
    boolean isSubsignature(ExecutableType m1, ExecutableType m2);

    /**
     * Returns the direct supertypes of a type.  The interface types, if any,
     * will appear last in the list.
     *
     * @param t  the type being examined
     * @return the direct supertypes, or an empty list if none
     * @throws IllegalArgumentException if given an executable or package type
     */
    List<? extends TypeMirror> directSupertypes(TypeMirror t);

    /**
     * Returns the erasure of a type.
     *
     * @param t  the type to be erased
     * @return the erasure of the given type
     * @throws IllegalArgumentException if given a package type
     * @jls 4.6 Type Erasure
     */
    TypeMirror erasure(TypeMirror t);

    /**
     * Returns the class of a boxed value of a given primitive type.
     * That is, <i>boxing conversion</i> is applied.
     *
     * @param p  the primitive type to be converted
     * @return the class of a boxed value of type {@code p}
     * @jls 5.1.7 Boxing Conversion
     */
    TypeElement boxedClass(PrimitiveType p);

    /**
     * Returns the type (a primitive type) of unboxed values of a given type.
     * That is, <i>unboxing conversion</i> is applied.
     *
     * @param t  the type to be unboxed
     * @return the type of an unboxed value of type {@code t}
     * @throws IllegalArgumentException if the given type has no
     *          unboxing conversion
     * @jls 5.1.8 Unboxing Conversion
     */
    PrimitiveType unboxedType(TypeMirror t);

    /**
     * Applies capture conversion to a type.
     *
     * @param t  the type to be converted
     * @return the result of applying capture conversion
     * @throws IllegalArgumentException if given an executable or package type
     * @jls 5.1.10 Capture Conversion
     */
    TypeMirror capture(TypeMirror t);

    /**
     * Returns a primitive type.
     *
     * @param kind  the kind of primitive type to return
     * @return a primitive type
     * @throws IllegalArgumentException if {@code kind} is not a primitive kind
     */
    PrimitiveType getPrimitiveType(TypeKind kind);

    /**
     * Returns the null type.  This is the type of {@code null}.
     *
     * @return the null type
     */
    NullType getNullType();

    /**
     * Returns a pseudo-type used where no actual type is appropriate.
     * The kind of type to return may be either
     * {@link TypeKind#VOID VOID} or {@link TypeKind#NONE NONE}.
     * For packages, use
     * {@link Elements#getPackageElement(CharSequence)}{@code .asType()}
     * instead.
     *
     * @param kind  the kind of type to return
     * @return a pseudo-type of kind {@code VOID} or {@code NONE}
     * @throws IllegalArgumentException if {@code kind} is not valid
     */
    NoType getNoType(TypeKind kind);

    /**
     * Returns an array type with the specified component type.
     *
     * @param componentType  the component type
     * @return an array type with the specified component type.
     * @throws IllegalArgumentException if the component type is not valid for
     *          an array
     */
    ArrayType getArrayType(TypeMirror componentType);

    /**
     * Returns a new wildcard type argument.  Either of the wildcard's
     * bounds may be specified, or neither, but not both.
     *
     * @param extendsBound  the extends (upper) bound, or {@code null} if none
     * @param superBound    the super (lower) bound, or {@code null} if none
     * @return a new wildcard
     * @throws IllegalArgumentException if bounds are not valid
     */
    WildcardType getWildcardType(TypeMirror extendsBound,
                                 TypeMirror superBound);

    /**
     * Returns the type corresponding to a type element and
     * actual type arguments.
     * Given the type element for {@code Set} and the type mirror
     * for {@code String},
     * for example, this method may be used to get the
     * parameterized type {@code Set<String>}.
     *
     * <p> The number of type arguments must either equal the
     * number of the type element's formal type parameters, or must be
     * zero.  If zero, and if the type element is generic,
     * then the type element's raw type is returned.
     *
     * <p> If a parameterized type is being returned, its type element
     * must not be contained within a generic outer class.
     * The parameterized type {@code Outer<String>.Inner<Number>},
     * for example, may be constructed by first using this
     * method to get the type {@code Outer<String>}, and then invoking
     * {@link #getDeclaredType(DeclaredType, TypeElement, TypeMirror...)}.
     *
     * @param typeElem  the type element
     * @param typeArgs  the actual type arguments
     * @return the type corresponding to the type element and
     *          actual type arguments
     * @throws IllegalArgumentException if too many or too few
     *          type arguments are given, or if an inappropriate type
     *          argument or type element is provided
     */
    DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs);

    /**
     * Returns the type corresponding to a type element
     * and actual type arguments, given a
     * {@linkplain DeclaredType#getEnclosingType() containing type}
     * of which it is a member.
     * The parameterized type {@code Outer<String>.Inner<Number>},
     * for example, may be constructed by first using
     * {@link #getDeclaredType(TypeElement, TypeMirror...)}
     * to get the type {@code Outer<String>}, and then invoking
     * this method.
     *
     * <p> If the containing type is a parameterized type,
     * the number of type arguments must equal the
     * number of {@code typeElem}'s formal type parameters.
     * If it is not parameterized or if it is {@code null}, this method is
     * equivalent to {@code getDeclaredType(typeElem, typeArgs)}.
     *
     * @param containing  the containing type, or {@code null} if none
     * @param typeElem    the type element
     * @param typeArgs    the actual type arguments
     * @return the type corresponding to the type element and
     *          actual type arguments, contained within the given type
     * @throws IllegalArgumentException if too many or too few
     *          type arguments are given, or if an inappropriate type
     *          argument, type element, or containing type is provided
     */
    DeclaredType getDeclaredType(DeclaredType containing,
                                 TypeElement typeElem, TypeMirror... typeArgs);

    /**
     * Returns the type of an element when that element is viewed as
     * a member of, or otherwise directly contained by, a given type.
     * For example,
     * when viewed as a member of the parameterized type {@code Set<String>},
     * the {@code Set.add} method is an {@code ExecutableType}
     * whose parameter is of type {@code String}.
     *
     * @param containing  the containing type
     * @param element     the element
     * @return the type of the element as viewed from the containing type
     * @throws IllegalArgumentException if the element is not a valid one
     *          for the given type
     */
    TypeMirror asMemberOf(DeclaredType containing, Element element);

    /**
     * Returns the annotations targeting the type.
     *
     * @param type the targeted type
     * @return the type annotations targeting the type
     */
    List<? extends AnnotationMirror> typeAnnotationsOf(TypeMirror type);

    /**
     * Returns the type's annotation for the specified type if
     * such an annotation is present, else {@code null}.  The
     * annotation has to be directly present on this
     * element.
     *
     * <p> The annotation returned by this method could contain an element
     * whose value is of type {@code Class}.
     * This value cannot be returned directly:  information necessary to
     * locate and load a class (such as the class loader to use) is
     * not available, and the class might not be loadable at all.
     * Attempting to read a {@code Class} object by invoking the relevant
     * method on the returned annotation
     * will result in a {@link MirroredTypeException},
     * from which the corresponding {@link TypeMirror} may be extracted.
     * Similarly, attempting to read a {@code Class[]}-valued element
     * will result in a {@link MirroredTypesException}.
     *
     * <blockquote>
     * <i>Note:</i> This method is unlike others in this and related
     * interfaces.  It operates on runtime reflective information &mdash;
     * representations of annotation types currently loaded into the
     * VM &mdash; rather than on the representations defined by and used
     * throughout these interfaces.  Consequently, calling methods on
     * the returned annotation object can throw many of the exceptions
     * that can be thrown when calling methods on an annotation object
     * returned by core reflection.  This method is intended for
     * callers that are written to operate on a known, fixed set of
     * annotation types.
     * </blockquote>
     *
     * @param <A>   the annotation type
     * @param type  the targeted type
     * @param annotationType  the {@code Class} object corresponding to
     *          the annotation type
     * @return the type's annotation for the specified annotation
     *         type if present on the type, else {@code null}
     *
     * @see Element#getAnnotationMirrors()
     * @see EnumConstantNotPresentException
     * @see AnnotationTypeMismatchException
     * @see IncompleteAnnotationException
     * @see MirroredTypeException
     * @see MirroredTypesException
     */
    <A extends Annotation> A typeAnnotationOf(TypeMirror type, Class<A> annotationType);

    /**
     * Returns the annotations targeting the method receiver type.
     *
     * @param type the targeted type
     * @return the receiver type of the executable type
     */
    TypeMirror receiverTypeOf(ExecutableType type);

    /**
     * Returns the type's annotation for the specified executable type
     * receiver if such an annotation is present, else {@code null}.  The
     * annotation has to be directly present on this
     * element.
     *
     * <p> The annotation returned by this method could contain an element
     * whose value is of type {@code Class}.
     * This value cannot be returned directly:  information necessary to
     * locate and load a class (such as the class loader to use) is
     * not available, and the class might not be loadable at all.
     * Attempting to read a {@code Class} object by invoking the relevant
     * method on the returned annotation
     * will result in a {@link MirroredTypeException},
     * from which the corresponding {@link TypeMirror} may be extracted.
     * Similarly, attempting to read a {@code Class[]}-valued element
     * will result in a {@link MirroredTypesException}.
     *
     * <blockquote>
     * <i>Note:</i> This method is unlike others in this and related
     * interfaces.  It operates on runtime reflective information &mdash;
     * representations of annotation types currently loaded into the
     * VM &mdash; rather than on the representations defined by and used
     * throughout these interfaces.  Consequently, calling methods on
     * the returned annotation object can throw many of the exceptions
     * that can be thrown when calling methods on an annotation object
     * returned by core reflection.  This method is intended for
     * callers that are written to operate on a known, fixed set of
     * annotation types.
     * </blockquote>
     *
     * @param <A>   the annotation type
     * @param type  the method type
     * @param annotationType  the {@code Class} object corresponding to
     *          the annotation type
     * @return the type's annotation for the specified annotation
     *         type if present on the type, else {@code null}
     *
     * @see Element#getAnnotationMirrors()
     * @see EnumConstantNotPresentException
     * @see AnnotationTypeMismatchException
     * @see IncompleteAnnotationException
     * @see MirroredTypeException
     * @see MirroredTypesException
     */
    // TODO: no longer needed?
    // <A extends Annotation> A receiverTypeAnnotationOf(ExecutableType type, Class<A> annotationType);

}
