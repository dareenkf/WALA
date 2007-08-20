/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.classLoader;

import com.ibm.wala.cfg.InducedCFG;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.Atom;
import com.ibm.wala.util.bytecode.BytecodeStream;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.debug.UnimplementedError;

/**
 * @author sfink
 * 
 */
public class SyntheticMethod implements IMethod {

  public final static SSAInstruction[] NO_STATEMENTS = new SSAInstruction[0];

  private final MethodReference method;
  
  private final IMethod resolvedMethod;

  private final IClass declaringClass;

  private final boolean isStatic;

  private final boolean isFactory;


  public SyntheticMethod(MethodReference method, IClass declaringClass, boolean isStatic, boolean isFactory) {
    super();
    this.method = method;
    this.resolvedMethod = null;
    this.declaringClass = declaringClass;
    this.isStatic = isStatic;
    this.isFactory = isFactory;
  }
  

  public SyntheticMethod(IMethod method, IClass declaringClass, boolean isStatic, boolean isFactory) {
    super();
    this.resolvedMethod = method;
    this.method = resolvedMethod.getReference();
    this.declaringClass = declaringClass;
    this.isStatic = isStatic;
    this.isFactory = isFactory;
  }

  /**
   * @see com.ibm.wala.classLoader.IMethod#isClinit()
   */
  public boolean isClinit() {
    return method.getSelector().equals(MethodReference.clinitSelector);
  }

  /**
   * @see com.ibm.wala.classLoader.IMethod#isInit()
   */
  public boolean isInit() {
    return method.getSelector().equals(MethodReference.initSelector);
  }

  /**
   * @see com.ibm.wala.classLoader.IMethod#isStatic()
   */
  public boolean isStatic() {
    return isStatic;
  }

  /**
   * @see com.ibm.wala.classLoader.IMethod#isNative()
   */
  public boolean isNative() {
    return false;
  }

  /**
   * @see com.ibm.wala.classLoader.IMethod#isAbstract()
   */
  public boolean isAbstract() {
    return false;
  }

  /**
   * @see com.ibm.wala.classLoader.IMethod#isPrivate()
   */
  public boolean isPrivate() {
    return false;
  }

  public boolean isProtected() {
    return false;
  }

  public boolean isPublic() {
    return false;
  }

  /**
   * @see com.ibm.wala.classLoader.IMethod#isFinal()
   */
  public boolean isFinal() {
    return false;
  }
  
  /**
   * @see com.ibm.wala.classLoader.IMethod#isVolatile()
   */
  public boolean isVolatile() {
    return false;
  }

  /**
   * @see com.ibm.wala.classLoader.IMethod#isAbstract()
   */
  public boolean isSynchronized() {
    return false;
  }

  /**
   * @see com.ibm.wala.classLoader.IMethod#isSynthetic()
   */
  public boolean isSynthetic() {
    return true;
  }

  /**
   * @see com.ibm.wala.classLoader.IMethod#getReference()
   */
  public MethodReference getReference() {
    return method;
  }

  public InducedCFG makeControlFlowGraph() {
    return new InducedCFG(getStatements(), this, Everywhere.EVERYWHERE);
  }

  public BytecodeStream getBytecodeStream() {
    Assertions.UNREACHABLE();
    return null;
  }

  /**
   * @see com.ibm.wala.classLoader.IMethod#getMaxLocals()
   */
  public int getMaxLocals() {
    throw new UnsupportedOperationException();
  }

  /**
   * @see com.ibm.wala.classLoader.IMethod#getMaxStackHeight()
   */
  public int getMaxStackHeight() {
    throw new UnsupportedOperationException();
  }

  /**
   * @see com.ibm.wala.classLoader.IMember#getDeclaringClass()
   */
  public IClass getDeclaringClass() {
    return declaringClass;
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass().equals(obj.getClass())) {
      SyntheticMethod other = (SyntheticMethod) obj;
      return (method.equals(other.method));
    } else {
      return false;
    }
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return method.hashCode();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer s = new StringBuffer("synthetic ");
    if (isFactoryMethod()) {
      s.append(" factory ");
    }
    s.append(method.toString());
    return s.toString();
  }

  /*
   * @see com.ibm.wala.classLoader.IMethod#hasExceptionHandler()
   */
  public boolean hasExceptionHandler() {
    return false;
  }

  public boolean hasPoison() {
    return false;
  }

  public String getPoison() {
    return null;
  }

  public byte getPoisonLevel() {
    return -1;
  }

  /**
   * @param options options governing SSA construction
   */
  public SSAInstruction[] getStatements(SSAOptions options) {
    return NO_STATEMENTS;
  }


  /**
   * Most subclasses should override this.
   * 
   * @param options options governing IR conversion
   */
  public IR makeIR(SSAOptions options) throws UnimplementedError {
    throw new UnimplementedError("haven't implemented IR yet for class " + getClass());
  }

  /*
   * @see com.ibm.wala.classLoader.IMethod#getParameterType(int)
   */
  public TypeReference getParameterType(int i) {
    if (isStatic()) {
      return method.getParameterType(i);
    } else {
      if (i == 0) {
        return method.getDeclaringClass();
      } else {
        return method.getParameterType(i - 1);
      }
    }
  }

  /**
   * 
   * @see com.ibm.wala.classLoader.IMethod#getNumberOfParameters()
   */
  public int getNumberOfParameters() {
    int n = method.getNumberOfParameters();
    return isStatic() ? n : n + 1;
  }

  /*
   * @see com.ibm.wala.classLoader.IMethod#getDeclaredExceptions()
   */
  public TypeReference[] getDeclaredExceptions() throws InvalidClassFileException {
    if (resolvedMethod == null) {
      return null;
    } else {
      return resolvedMethod.getDeclaredExceptions();
    }
  }

  public Atom getName() {
    return method.getSelector().getName();
  }

  public Descriptor getDescriptor() {
    return method.getSelector().getDescriptor();
  }

  /*
   * @see com.ibm.wala.classLoader.IMethod#getLineNumber(int)
   */
  public int getLineNumber(int bcIndex) {
    return -1;
  }

  public boolean isFactoryMethod() {
    return isFactory;
  }

  /*
   * @see com.ibm.wala.classLoader.IMethod#getSignature()
   */
  public String getSignature() {
    return getReference().getSignature();
  }

  /*
   * @see com.ibm.wala.classLoader.IMethod#getSelector()
   */
  public Selector getSelector() {
    return getReference().getSelector();
  }

  /*
   * @see com.ibm.wala.classLoader.IMethod#getLocalVariableName(int, int)
   */
  public String getLocalVariableName(int bcIndex, int localNumber) {
    // no information is available
    return null;
  }

  /*
   * @see com.ibm.wala.classLoader.IMethod#hasLocalVariableTable()
   */
  public boolean hasLocalVariableTable() {
    return false;
  }

  public SSAInstruction[] getStatements() {
    return getStatements(SSAOptions.defaultOptions());
  }


  /*
   * @see com.ibm.wala.classLoader.IMethod#getReturnType()
   */
  public TypeReference getReturnType() {
    return getReference().getReturnType();
  }


  public IClassHierarchy getClassHierarchy() {
    return getDeclaringClass().getClassHierarchy();
  }

}
