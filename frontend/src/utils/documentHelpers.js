/** Flatten nested section tree for table of contents */
export function flattenSections(sections, level = 0) {
  if (!sections?.length) return [];

  const result = [];
  for (const section of sections) {
    result.push({ ...section, level });
    if (section.children?.length) {
      result.push(...flattenSections(section.children, level + 1));
    }
  }
  return result;
}

/** Normalize relations API response to grouped list */
export function normalizeRelations(data) {
  if (!data) return [];

  if (data.groups?.length) return data.groups;

  const relations = data.relations || data;
  if (Array.isArray(relations)) {
    return relations.map((item) => ({
      relationType: item.relationType,
      documents: [item.document || item.targetDocument].filter(Boolean),
    }));
  }

  const groups = [];
  const typeMap = {
    guidedBy: 'GUIDED_BY',
    guides: 'GUIDES',
    amendedBy: 'AMENDED_BY',
    amends: 'AMENDS',
    replaces: 'REPLACES',
    replacedBy: 'REPLACED_BY',
    related: 'RELATED',
  };

  Object.entries(typeMap).forEach(([key, relationType]) => {
    const items = relations[key];
    if (!items?.length) return;
    groups.push({
      relationType,
      documents: items.map((item) => item.document || item),
    });
  });

  return groups;
}

/** Format AI summary object to display text */
export function formatAiSummary(summary) {
  if (!summary) return '';
  if (typeof summary === 'string') return summary;

  const parts = [];
  if (summary.mainPoints?.length) {
    parts.push(summary.mainPoints.map((p, i) => `${i + 1}. ${p}`).join('\n'));
  }
  if (summary.applicableSubjects) {
    parts.push(`Đối tượng áp dụng: ${summary.applicableSubjects}`);
  }
  if (summary.mainContent) {
    parts.push(summary.mainContent);
  }
  return parts.join('\n\n');
}
