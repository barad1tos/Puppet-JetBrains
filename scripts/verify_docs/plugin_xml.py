"""plugin.xml descriptor and Marketplace change-note parser."""

from __future__ import annotations

import html
import re
import xml.etree.ElementTree as ET
from dataclasses import dataclass

from .paths import PLUGIN_XML


@dataclass(frozen=True)
class ChangeNoteEntry:
    version: str
    body: str


@dataclass(frozen=True)
class PluginXmlMetadata:
    release_date: str | None
    release_version: str | None
    description: str
    change_notes: str
    change_note_entries: list[ChangeNoteEntry]


def load_plugin_xml_metadata() -> PluginXmlMetadata:
    # The parsed file is this repository's own plugin.xml, a trusted input.
    # nosemgrep: python.lang.security.use-defused-xml-parse.use-defused-xml-parse -- trusted repo file
    root = ET.parse(PLUGIN_XML).getroot()
    # <product-descriptor> exists only for paid plugins; a free plugin has no release-date/version.
    product_descriptor = root.find("product-descriptor")

    change_notes = root.findtext("change-notes") or ""
    return PluginXmlMetadata(
        release_date=product_descriptor.get("release-date") if product_descriptor is not None else None,
        release_version=product_descriptor.get("release-version") if product_descriptor is not None else None,
        description=root.findtext("description") or "",
        change_notes=change_notes,
        change_note_entries=parse_change_note_entries(change_notes),
    )


def parse_change_note_entries(change_notes: str) -> list[ChangeNoteEntry]:
    headings = list(
        re.finditer(
            r"<h3>\s*(?P<version>[^<]+?)\s*</h3>",
            change_notes,
            re.IGNORECASE,
        ),
    )
    entries: list[ChangeNoteEntry] = []
    for index, heading in enumerate(headings):
        body_start = heading.end()
        body_end = headings[index + 1].start() if index + 1 < len(headings) else len(change_notes)
        entries.append(
            ChangeNoteEntry(
                version=html.unescape(heading.group("version").strip()),
                body=change_notes[body_start:body_end],
            ),
        )
    return entries
