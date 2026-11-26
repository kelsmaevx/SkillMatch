const RESULT = JSON.parse(sessionStorage.getItem('skillmatch.result') || 'null');
const PAGE = decodeURIComponent(location.pathname).toLowerCase();
if (!RESULT) location.replace('upload file.html');
const esc = s => String(s).replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));

if (RESULT && PAGE.includes('matches')) {
  const main = document.querySelector('main'), sub = main.querySelector('.section-heading p');
  if (!RESULT.techRelated) {
    main.querySelectorAll('.matches-grid').forEach(n => n.remove());
    sub.textContent = 'This resume does not look technology-related, so there are no tech matches to show. Add your tech skills and try again.';
  } else {
    sub.textContent = 'Your Top 5 Matches based on your qualifications.' + (RESULT.usedAi ? ' (Resume read with AI)' : ' (Resume read with keyword matching)');
    document.querySelectorAll('.result-card').forEach((card, i) => {
      const r = RESULT.results[i]; if (!r) { card.style.display = 'none'; return; }
      card.querySelector('h2').textContent = r.title;
      card.querySelector('.score-percent').textContent = r.score + '%';
      const b = card.querySelector('.status-badge'); b.textContent = r.level; b.className = 'status-badge ' + (r.score >= 80 ? 'strong' : 'good');
    });
  }
}
if (RESULT && PAGE.includes('skill gaps')) {
  const g = RESULT.gaps, list = document.querySelector('.gaps-list');
  list.innerHTML = g.items.length ? g.items.map((i, n) => `<div class="gap-item"><div class="gap-num">${n + 1}</div><div class="gap-body"><h3>${esc(i.skill)} <span class="freq">(Missing in ${i.missingIn} of ${i.outOf} matches)</span></h3><p>${esc(i.advice)}</p></div></div>`).join('') : '<p>No missing skills found across your top matches.</p>';
  document.querySelector('.summary-panel p').textContent = g.summary || '';
}
if (RESULT && PAGE.includes('view analysis')) {
  const r = RESULT.results[(Number(new URLSearchParams(location.search).get('rank')) || 1) - 1] || RESULT.results[0];
  if (r) {
    const C = 301.6;
    document.querySelector('.rationale-box h3').textContent = r.title;
    document.querySelector('.donut-text').textContent = r.score + '%';
    document.querySelector('.donut-fill').setAttribute('stroke-dashoffset', (C * (1 - r.score / 100)).toFixed(1));
    document.querySelector('.compat-value').textContent = r.level;
    document.querySelector('.rationale-box p').textContent = r.explanation;
    const cols = document.querySelectorAll('.skills-split > div');
    const chips = (items, cls) => items.length ? items.map(s => `<span class="skill-chip ${cls}">${esc(s)}</span>`).join('') : '<span>None</span>';
    cols[0].innerHTML = '<h4>Matched Skills</h4>' + chips(r.matched, 'matched');
    cols[1].innerHTML = '<h4>Missing Skills</h4>' + chips(r.missing, 'missing');
    if (r.training.length) document.querySelector('.rationale-box').insertAdjacentHTML('beforeend', '<h4 class="steps-title">Recommended next steps</h4><ul class="steps">' + r.training.map(t => `<li>${esc(t)}</li>`).join('') + '</ul>');
  }
}
