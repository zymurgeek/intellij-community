// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.sdk.flavors;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PatternUtil;
import com.jetbrains.python.psi.LanguageLevel;
import icons.PythonIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author traff
 */
public class PyPySdkFlavor extends PythonSdkFlavor {
  public static PyPySdkFlavor INSTANCE = new PyPySdkFlavor();

  private static final Pattern VERSION_RE = Pattern.compile("\\[(PyPy \\S+).*\\]");
  private static final Pattern PYTHON_VERSION_RE = Pattern.compile("(Python \\S+).*");
  private static final Pattern VERSION_STRING_RE = Pattern.compile("PyPy (\\S+)( \\[Python (\\S+)\\])?");

  private PyPySdkFlavor() {
  }

  @Override
  public boolean isValidSdkPath(@NotNull File file) {
    return StringUtil.toLowerCase(FileUtil.getNameWithoutExtension(file)).startsWith("pypy");
  }

  @Nullable
  @Override
  public String getVersionStringFromOutput(@NotNull String output) {
    final List<String> lines = Arrays.asList(StringUtil.splitByLines(output));
    final String version = PatternUtil.getFirstMatch(lines, VERSION_RE);
    final String pythonVersion = PatternUtil.getFirstMatch(lines, PYTHON_VERSION_RE);
    if (version != null) {
      final StringBuilder builder = new StringBuilder();
      builder.append(version);
      if (pythonVersion != null) {
        builder.append(" [");
        builder.append(pythonVersion);
        builder.append("]");
      }
      return builder.toString();
    }
    return null;
  }

  @Override
  public String getVersionOption() {
    return "--version";
  }

  @NotNull
  @Override
  public String getName() {
    return "PyPy";
  }

  @NotNull
  @Override
  public LanguageLevel getLanguageLevel(@NotNull Sdk sdk) {
    final String versionString = sdk.getVersionString();
    if (versionString != null) {
      final Matcher matcher = VERSION_STRING_RE.matcher(versionString);
      if (matcher.matches()) {
        final String version = matcher.group(1);
        final String pythonVersion = matcher.group(3);
        if (pythonVersion != null) {
          return LanguageLevel.fromPythonVersion(pythonVersion);
        }
        else if (version != null) {
          return LanguageLevel.fromPythonVersion(getPythonVersion(version));
        }
      }
    }
    return LanguageLevel.getDefault();
  }

  private static String getPythonVersion(@NotNull String pypyVersion) {
    final String DEFAULT = "2.4";
    final String LATEST = "2.7";
    final List<String> vs = StringUtil.split(pypyVersion, ".");
    try {
      if (vs.size() >= 2) {
        final int major = Integer.parseInt(vs.get(0));
        final int minor = Integer.parseInt(vs.get(1));
        if (major > 1) {
          return LATEST;
        }
        else if (major == 1) {
          if (minor >= 5) {
            return "2.7";
          }
          else if (minor >= 4) {
            return "2.5";
          }
        }
      }
    }
    catch (NumberFormatException ignored) {
    }
    return DEFAULT;
  }

  @Override
  public Icon getIcon() {
    return PythonIcons.Python.Pypy;
  }
}
