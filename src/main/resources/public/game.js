async function startGame() {
  await fetch('/start', { method: 'POST' });
  loadScene();
}

async function loadScene() {
  const res = await fetch('/scene');
  const data = await res.json();
  document.getElementById('scene').innerText = data.description;
  const choicesDiv = document.getElementById('choices');
  choicesDiv.innerHTML = '';
  document.getElementById('ending').innerText = '';
  document.getElementById('restart').style.display = 'none';

  if (data.ending) {
    document.getElementById('ending').innerText = 'Ending: ' + data.ending;
    document.getElementById('restart').style.display = '';
    return;
  }

  for (const choice of data.choices) {
    const btn = document.createElement('button');
    btn.innerText = choice;
    btn.onclick = async () => {
      await fetch('/choice', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'choice=' + encodeURIComponent(choice)
      });
      loadScene();
    };
    choicesDiv.appendChild(btn);
  }
}

document.getElementById('restart').onclick = startGame;

// Start game on page load
startGame();