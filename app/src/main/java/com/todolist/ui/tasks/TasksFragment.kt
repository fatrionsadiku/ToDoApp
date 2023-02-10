package com.todolist.ui.tasks

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.todolist.data.SortOrder
import com.todolist.data.Task
import com.todolist.mvvmtodo.R
import com.todolist.mvvmtodo.databinding.FragmentTasksBinding
import com.todolist.util.exhaustive
import com.todolist.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(), TasksAdapter.OnItemClickListener {
    lateinit var binding : FragmentTasksBinding
    private val viewModel : TasksViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTasksBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val taskAdapter = TasksAdapter(this)

        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwipe(task)
                }
            }).attachToRecyclerView(recyclerViewTasks)

            fab.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }

        viewModel.tasks.observe(viewLifecycleOwner){
            taskAdapter.submitList(it)
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvent.collect { event ->
                when(event){
                    is TasksViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                       Snackbar.make(requireView(),"Task Deleted Succesfully", Snackbar.LENGTH_LONG)
                           .setAction("Undo") {
                                viewModel.onUndoDeleteClick(event.task)
                           }.show()
                    }
                    is TasksViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(title = "Add a new task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TasksEvent.NavigateToAddedTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(event.task, "Edit current task")
                        findNavController().navigate(action)
                    }
                }

            }
        }

        setHasOptionsMenu(true)

    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task,isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

        val searchItem = menu.findItem(R.id.actionSearch)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.actionHideCompletedTasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.actionSortByName -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
            }

            R.id.actionSortByDateCreated -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
            }

            R.id.actionHideCompletedTasks -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)            }

            R.id.actionDeleteAllCompletedTasks -> {

            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

}