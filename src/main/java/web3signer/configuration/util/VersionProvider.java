/*
 * Copyright 2023 Usman Saleem.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package web3signer.configuration.util;

import picocli.CommandLine.IVersionProvider;

public class VersionProvider implements IVersionProvider {
  private static final String CLIENT_IDENTITY = "signer-configuration-generator";
  private static final String[] VERSION =
      new String[] {
        CLIENT_IDENTITY
            + "/v"
            + VersionProvider.class.getPackage().getImplementationVersion()
            + "/"
            + PlatformDetector.getOS()
            + "/"
            + PlatformDetector.getVM()
      };

  @Override
  public String[] getVersion() {
    return VERSION;
  }
}
