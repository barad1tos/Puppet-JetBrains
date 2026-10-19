# Privacy Policy

Last updated: October 19, 2026

Puppet Tools does not sell, share, or transmit personal data to external services.

The plugin runs locally inside JetBrains IDEs. It reads the manifests, templates and Hiera data files of the open project to provide completion, navigation and inspections, and keeps that information in the IDE's local indexes on the user's machine.

When external validation is enabled (it is on by default), the plugin writes the text of the manifest being edited to a temporary file in the system temporary directory, runs the configured `puppet` (or `openvox`) and `puppet-lint` executables on it, reads their output and deletes the file. The executables are found on `PATH` or configured explicitly under Settings | Tools | Puppet, and both checks can be disabled there. Nothing is sent anywhere by the plugin itself.

The plugin does not operate external analytics, telemetry, advertising, or tracking services.

Plugin installation, updates and Marketplace account data are handled by JetBrains IDEs and JetBrains Marketplace and are governed by JetBrains' own privacy policies.

Issue reports, support requests, Marketplace reviews, and other user-submitted messages are handled through GitHub and JetBrains Marketplace. Those services are governed by their own privacy policies.
