/*
 * Copyright (C) 2016 Pluralsight, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package hydra.spark.dsl.util

import scala.reflect.runtime.universe._
import scala.reflect.runtime.{ currentMirror => cm, universe => ru }

/**
 * Created by alexsilva on 1/4/16.
 */
object ReflectionUtils {

  private lazy val universeMirror = ru.runtimeMirror(getClass.getClassLoader)

  def instantiateType[M: TypeTag](args: List[Any]): M = {
    val cl = typeOf[M].typeSymbol.asClass
    instance(cl, args)
  }

  def instantiateClass[M](cls: Class[M], args: List[Any]): M = {
    val cl = cm.classSymbol(cls)
    instance(cl, args)
  }

  def companionOf[T](implicit tt: ru.TypeTag[T]) = {
    val companionMirror = universeMirror.reflectModule(ru.typeOf[T].typeSymbol.companionSymbol.asModule)
    companionMirror.instance
  }

  def companionOf[T](cls: Class[T]) = {
    val companionMirror = universeMirror.reflectModule(getType(cls).typeSymbol.companionSymbol.asModule)
    companionMirror.instance.asInstanceOf[T]
  }

  def getType[T](clazz: Class[T]) = universeMirror.classSymbol(clazz).toType

  def instantiateClass[M: TypeTag](clazz: String, args: List[Any]): M = {
    val cl = cm.classSymbol(Class.forName(clazz))
    instance(cl, args)
  }

  private def instance[M: TypeTag](cl: ClassSymbol, args: List[Any]): M = {
    val clazz = cm.reflectClass(cl)
    val ctor = cl.toType.declaration(reflect.runtime.universe.nme.CONSTRUCTOR).asMethod
    val ctorm = clazz.reflectConstructor(ctor)
    val obj = ctorm(args: _*).asInstanceOf[M]
    obj
  }
}

