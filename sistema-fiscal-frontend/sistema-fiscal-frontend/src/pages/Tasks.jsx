import React, { useState } from "react";
import TaskList from "../components/TaskList";
import TaskForm from "../components/TaskForm";
import '../styles/pages/Tasks.css';

export default function Tasks() {
  const [editingTask, setEditingTask] = useState(null);
  const [refresh, setRefresh] = useState(false);

  const handleEdit = (task) => setEditingTask(task);
  const handleSaved = () => {
    setEditingTask(null);
    setRefresh((r) => !r);
  };
  const handleCancel = () => setEditingTask(null);

  return (
    <div className="card" style={{ maxWidth: 600, margin: "2rem auto" }}>
      <h2 className="page-title">Minhas Tasks</h2>
      <TaskForm
        task={editingTask}
        onSaved={handleSaved}
        onCancel={editingTask ? handleCancel : undefined}
      />
      <hr style={{ margin: "2rem 0" }} />
      <TaskList key={refresh} onEdit={handleEdit} />
    </div>
  );
}