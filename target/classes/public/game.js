async function startGame() {
  await fetch('/start', { method: 'POST' });
  loadScene();
}

async function loadScene() {
  const res = await fetch('/scene');
  const data = await res.json();
  const sceneDiv = document.getElementById('scene');
  const choicesDiv = document.getElementById('choices');
  sceneDiv.innerText = data.description;
  choicesDiv.innerHTML = '';
  document.getElementById('ending').innerText = '';
  document.getElementById('restart').style.display = 'none';

  // Show feedback message
  if (data.lastMessage) {
    const msg = document.createElement('div');
    msg.innerText = data.lastMessage;
    msg.style.margin = "1em 0";
    msg.style.color = "#9cf";
    choicesDiv.appendChild(msg);
  }

  // Combat UI
  if (data.inCombat && data.enemy) {
    const enemyDiv = document.createElement('div');
    enemyDiv.innerHTML = `<b>Enemy:</b> ${data.enemy.name} | <b>HP:</b> ${data.enemy.health}`;
    choicesDiv.appendChild(enemyDiv);

    // Attack button
    const attackBtn = document.createElement('button');
    attackBtn.innerText = "Attack";
    attackBtn.onclick = async () => {
      await fetch('/combat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=attack&itemIndex=-1'
      });
      loadScene();
    };
    choicesDiv.appendChild(attackBtn);

    // Use item buttons
    if (data.inventory && data.inventory.length > 0) {
      data.inventory.forEach((item, idx) => {
        if (item.type === "Consumable") {
          const btn = document.createElement('button');
          btn.innerText = `Use ${item.name}`;
          btn.onclick = async () => {
            await fetch('/combat', {
              method: 'POST',
              headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
              body: `action=useItem&id=${encodeURIComponent(item.id)}`
            });
            loadScene();
          };
          choicesDiv.appendChild(btn);
        }
      });
    }

    // Run button
    const runBtn = document.createElement('button');
    runBtn.innerText = "Run";
    runBtn.onclick = async () => {
      await fetch('/combat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=run&itemIndex=-1'
      });
      loadScene();
    };
    choicesDiv.appendChild(runBtn);

    // Show inventory summary
    showInventory(data.inventory, data.playerHealth, data.playerMaxHealth, data.playerName);
    return;
  }

  // Intro scene: ask for name
  if (data.scene === "Intro") {
    const input = document.createElement('input');
    input.type = "text";
    input.placeholder = "Enter your name";
    input.id = "nameInput";
    choicesDiv.appendChild(input);
    const btn = document.createElement('button');
    btn.innerText = "Continue";
    btn.onclick = async () => {
      const name = document.getElementById('nameInput').value;
      await fetch('/set-name', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'name=' + encodeURIComponent(name)
      });
      loadScene();
    };
    choicesDiv.appendChild(btn);
    return;
  }

  // Backpack button
  const backpackBtn = document.createElement('button');
  backpackBtn.innerText = "Open Backpack";
  backpackBtn.onclick = showBackpack;
  choicesDiv.appendChild(backpackBtn);

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

  // Show inventory summary
  showInventory(data.inventory, data.playerHealth, data.playerMaxHealth, data.playerName);
}

function showInventory(inventory, health, maxHealth, playerName) {
  const invDiv = document.getElementById('backpack');
  invDiv.innerHTML = `<b>${playerName ? playerName : "Player"}</b> | <b>Health:</b> ${health}/${maxHealth}<br><b>Backpack:</b> `;
  if (!inventory || inventory.length === 0) {
    invDiv.innerHTML += "Empty";
    return;
  }
  invDiv.innerHTML += inventory.map(i => i.name).join(", ");
}

async function showBackpack() {
  const res = await fetch('/scene');
  const data = await res.json();
  const invDiv = document.getElementById('choices');
  invDiv.innerHTML = "<b>Backpack:</b><br>";

  // Show feedback message if present
  if (data.lastMessage) {
    const msg = document.createElement('div');
    msg.innerText = data.lastMessage;
    msg.style.margin = "1em 0";
    msg.style.color = "#9cf";
    invDiv.appendChild(msg);
  }

  if (!data.inventory || data.inventory.length === 0) {
    invDiv.innerHTML += "Your backpack is empty.<br>";
  } else {
    data.inventory.forEach((item, idx) => {
      if (idx === data.equippedWeaponIndex) {
        invDiv.innerHTML += `<b>${item.name} (equipped)</b> - ${item.description}<br>`;
      } else if (item.type === "Weapon") {
        // Show equip button for weapons
        const btn = document.createElement('button');
        btn.innerText = `Equip ${item.name}`;
        btn.onclick = async () => {
          await fetch('/equip-weapon', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'id=' + encodeURIComponent(item.id)
          });
          showBackpack();
        };
        invDiv.appendChild(btn);
        invDiv.appendChild(document.createTextNode(` - ${item.description}`));
        invDiv.appendChild(document.createElement('br'));
      } else if (item.type === "Consumable" || item.name === "Device") {
        // Show use button for consumables and device
        const btn = document.createElement('button');
        btn.innerText = item.name === "Device" ? `Use Device` : `Use ${item.name}`;
        btn.onclick = async () => {
          await fetch('/use-item', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'id=' + encodeURIComponent(item.id)
          });
          showBackpack();
        };
        invDiv.appendChild(btn);
        invDiv.appendChild(document.createTextNode(` - ${item.description}`));
        invDiv.appendChild(document.createElement('br'));
      } else if (item.name === "Shuttle Parts") {
        invDiv.innerHTML += `${item.name} - ${item.description}<br>`;
      }
    });
  }

  // Add a back button
  const backBtn = document.createElement('button');
  backBtn.innerText = "Back";
  backBtn.onclick = loadScene;
  invDiv.appendChild(backBtn);
}

document.getElementById('restart').onclick = startGame;

// Start game on page load
startGame();
