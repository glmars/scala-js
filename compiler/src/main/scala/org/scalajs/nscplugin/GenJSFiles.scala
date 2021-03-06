/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package org.scalajs.nscplugin

import scala.tools.nsc._
import scala.tools.nsc.io.AbstractFile
import scala.reflect.internal.pickling.PickleBuffer

import java.io._

import org.scalajs.ir

/** Send JS ASTs to files
 *
 *  @author Sébastien Doeraene
 */
trait GenJSFiles extends SubComponent { self: GenJSCode =>
  import global._
  import jsAddons._

  def genIRFile(cunit: CompilationUnit, sym: Symbol, suffix: Option[String],
      tree: ir.Trees.ClassDef): Unit = {
    val outfile = getFileFor(cunit, sym, suffix.getOrElse("") + ".sjsir")
    val output = outfile.bufferedOutput
    try ir.Serializers.serialize(output, tree)
    finally output.close()
  }

  private def getFileFor(cunit: CompilationUnit, sym: Symbol,
      suffix: String) = {
    val baseDir: AbstractFile =
      settings.outputDirs.outputDirFor(cunit.source.file)

    val fullName = sym.fullName match {
      case "java.lang._String" => "java.lang.String"
      case fullName            => fullName
    }

    val pathParts = fullName.split("[./]")
    val dir = (baseDir /: pathParts.init)(_.subdirectoryNamed(_))

    var filename = pathParts.last
    if (sym.isModuleClass && !sym.isImplClass)
      filename = filename + nme.MODULE_SUFFIX_STRING

    dir fileNamed (filename + suffix)
  }
}
