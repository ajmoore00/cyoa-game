function setTheme(sceneKey) {
  document.body.className = '';
  if (["CryoWake", "Storage", "Bridge", "Workshop", "Medbay", "SpareParts"].includes(sceneKey)) {
    document.body.classList.add('theme-ship');
  } else if (["CrashSite", "Tracks"].includes(sceneKey)) {
    document.body.classList.add('theme-outside');
  } else if (["ShuttleBay", "ShuttleBayFixed"].includes(sceneKey)) {
    document.body.classList.add('theme-shuttle');
  } else if (["LargeCreatureEncounter", "PostCombatLarge"].includes(sceneKey)) {
    document.body.classList.add('theme-beast');
  } else if (["MirageWalk", "MirageRest", "MirageRestScene"].includes(sceneKey)) {
    document.body.classList.add('theme-mirage');
  } else if (["AlienChamber", "AlienTalk"].includes(sceneKey)) {
    document.body.classList.add('theme-alien');
  } else {
    document.body.classList.add('theme-ship');
  }
}

function createActionButton(text, handler) {
  const btn = document.createElement('button');
  btn.innerText = text;
  btn.onclick = async (e) => {
    btn.disabled = true;
    await handler(e);
    btn.disabled = false;
  };
  return btn;
}

async function startGame() {
  await fetch('/start', { method: 'POST' });
  loadScene();
}

async function loadScene() {
  const res = await fetch('/scene');
  const data = await res.json();
  renderScene(data);
}

function renderScene(data) {
  setTheme(data.scene);
  const sceneDiv = document.getElementById('scene');
  const choicesDiv = document.getElementById('choices');
  sceneDiv.innerText = data.description;
  choicesDiv.innerHTML = '';
  document.getElementById('ending').innerText = '';
  document.getElementById('restart').style.display = 'none';

  // Show feedback message FIRST
  if (data.lastMessage && data.lastMessage.trim() !== "") {
    const msg = document.createElement('div');
    msg.innerText = data.lastMessage;
    msg.className = "feedback-message";
    choicesDiv.appendChild(msg);
  }

  // Combat UI
  if (data.inCombat && data.enemy) {
    const enemyDiv = document.createElement('div');
    enemyDiv.innerHTML = `<b>Enemy:</b> ${data.enemy.name} | <b>HP:</b> ${data.enemy.health}`;
    choicesDiv.appendChild(enemyDiv);

    // Attack button
    choicesDiv.appendChild(createActionButton("Attack", async () => {
      const res = await fetch('/combat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=attack&itemIndex=-1'
      });
      const data = await res.json();
      renderScene(data);
    }));

    // Use item buttons
    if (data.inventory && data.inventory.length > 0) {
      data.inventory.forEach((item) => {
        if (item.type === "Consumable") {
          choicesDiv.appendChild(createActionButton(`Use ${item.name}`, async () => {
            const res = await fetch('/use-item', {
              method: 'POST',
              headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
              body: 'id=' + encodeURIComponent(item.id)
            });
            const data = await res.json();
            renderScene(data);
          }));
        }
      });
    }

    // Run button
    choicesDiv.appendChild(createActionButton("Run", async () => {
      const res = await fetch('/combat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=run&itemIndex=-1'
      });
      const data = await res.json();
      renderScene(data);
    }));

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
    choicesDiv.appendChild(createActionButton("Continue", async () => {
      const name = document.getElementById('nameInput').value;
      const res = await fetch('/set-name', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'name=' + encodeURIComponent(name)
      });
      const data = await res.json();
      renderScene(data);
    }));
    return;
  }

  // Backpack button
  choicesDiv.appendChild(createActionButton("Open Backpack", async () => {
    const res = await fetch('/scene');
    const data = await res.json();
    showBackpack(data);
  }));

  if (data.ending) {
    document.getElementById('ending').innerText = 'Ending: ' + data.ending;
    document.getElementById('restart').style.display = '';
    return;
  }

  // Scene choices
  for (const choice of data.choices) {
    choicesDiv.appendChild(createActionButton(choice, async () => {
      const res = await fetch('/choice', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'choice=' + encodeURIComponent(choice)
      });
      const data = await res.json();
      renderScene(data);
    }));
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

async function showBackpack(data) {
  if (!data) throw new Error("showBackpack requires data to be passed in.");
  const invDiv = document.getElementById('choices');
  // Force full clear
  while (invDiv.firstChild) invDiv.removeChild(invDiv.firstChild);
  invDiv.innerHTML = "<b>Backpack:</b><br>";

  // Show feedback message if present
  if (data.lastMessage && data.lastMessage.trim() !== "") {
    const msg = document.createElement('div');
    msg.innerText = data.lastMessage;
    msg.className = "feedback-message";
    invDiv.appendChild(msg);
  }

  if (!data.inventory || data.inventory.length === 0) {
    invDiv.innerHTML += "Your backpack is empty.<br>";
  } else {
    data.inventory.forEach((item, idx) => {
      if (idx === data.equippedWeaponIndex) {
        invDiv.innerHTML += `<b>${item.name} (equipped)</b> - ${item.description}<br>`;
      } else if (item.type === "Weapon") {
        invDiv.appendChild(createActionButton(`Equip ${item.name}`, async () => {
          const res = await fetch('/equip-weapon', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'id=' + encodeURIComponent(item.id)
          });
          const data = await res.json();
          console.log("Inventory after action:", data.inventory.map(i => ({name: i.name, id: i.id})));
          showBackpack(data);
        }));
        invDiv.appendChild(document.createTextNode(` - ${item.description}`));
        invDiv.appendChild(document.createElement('br'));
      } else if (item.type === "Consumable" || item.name === "Device") {
        invDiv.appendChild(createActionButton(item.name === "Device" ? `Use Device` : `Use ${item.name}`, async () => {
          const res = await fetch('/use-item', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'id=' + encodeURIComponent(item.id)
          });
          const data = await res.json();
          console.log("Inventory after action:", data.inventory.map(i => ({name: i.name, id: i.id})));
          if (data.ending) {
            renderScene(data); // Immediately show ending if set
          } else {
            showBackpack(data); // Otherwise, stay in backpack
          }
        }));
        invDiv.appendChild(document.createTextNode(` - ${item.description}`));
        invDiv.appendChild(document.createElement('br'));
      } else if (item.name === "Shuttle Parts") {
        invDiv.innerHTML += `${item.name} - ${item.description}<br>`;
      }
    });
  }

  // Add a back button
  invDiv.appendChild(createActionButton("Back", loadScene));
}

document.getElementById('restart').onclick = startGame;
startGame();
